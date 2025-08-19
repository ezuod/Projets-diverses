libname x xlsx '/home/u64300920/data_test_BIFORA (2) (1).xlsx';

/*Import feuille Data_1*/
proc import datafile='/home/u64300920/data_test_BIFORA (2) (1).xlsx'
    out=x.data1
    dbms=xlsx
    replace;
    sheet="Data_1";
    getnames=yes;
run;

/*Import feuille Data_2*/
proc import datafile='/home/u64300920/data_test_BIFORA (2) (1).xlsx'
    out=x.data2
    dbms=xlsx
    replace;
    sheet="Data_2";
    getnames=yes;
run;

/* Toute cette partie ne marche pas */
/* Renomer "identiffiant" */
proc datasets library=x nolist;
    modify data1;
    rename identiffiant = identifiant;
quit; 

/* Création d'une nouvelle version de data1 avec conversion */
/* ici on ne change que Epargne_financiere mais il faut faire la même chose pour Epargne_totale et DATE_ERG */
data x.data1_tmp;
    set x.data1;
    Epargne_financiere_tmp = input(Epargne_financiere, best32.);
    drop Epargne_financiere;   /* supprime l’ancienne variable caractère */
    rename Epargne_financiere_tmp = Epargne_financiere;
run;

/* Remplace par la nouvelle version */
proc datasets library=x nolist;
    delete data1;                   /* supprime l’ancienne */
    change data1_tmp = data1;       /* renomme la nouvelle en data1 */
quit;

/* Merge de data1 et data2 */
/* Trier par identifiant */
proc sort data=x.data1;
    by identifiant;
run;

proc sort data=x.data2;
    by identifiant;
run;

/* Fusion */
data x.data_merge;
    merge x.data1(in=a) x.data2(in=b);
    by identifiant;
    if a and b;
run;

/* fin partie */

/* Vérification import */
proc contents data=x.data1;
    title "Structure du dataset DATA_1";
run;

proc contents data=x.data2;
    title "Structure du dataset DATA_2";
run;
