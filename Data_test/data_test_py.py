#!/usr/bin/env python3
# -*- coding: utf-8 -*-
# Créé le 15 août 2025
# Auteur : wendyr

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

# Importation des 3 feuilles Excel
path = r"/Users/wendyr/Downloads/data_test_BIFORA (2) (1).xlsx"
data1 = pd.read_excel(path, sheet_name="Data_1")
data2 = pd.read_excel(path, sheet_name="Data_2")
dict_vars = pd.read_excel(path, sheet_name="TYPES VARIABLES ")

# Standardisation des noms de colonnes
data1 = data1.rename(columns={'identiffiant ': 'identifiant'})  # correction d'un nom de colonne
data1.columns = data1.columns.str.strip()  # suppression des espaces
data2.columns = data2.columns.str.strip()
dict_vars.columns = [c.strip() for c in dict_vars.columns]
dict_vars['VARIABLES'] = dict_vars['VARIABLES'].str.strip()

# Conversion des variables en fonction de la feuille TYPES VARIABLES
for idx, row in dict_vars.iterrows():
    var = row['VARIABLES']
    coltype = row.get('Quali / Quanti ') if 'Quali / Quanti ' in dict_vars.columns else row.get('Quali / Quanti ')
    if var in data1.columns and isinstance(coltype, str) and coltype == 'Quanti':
        data1[var] = pd.to_numeric(data1[var], errors='coerce')  # conversion en numérique
    if var in data2.columns and isinstance(coltype, str) and coltype == 'Quanti':
        data2[var] = pd.to_numeric(data2[var], errors='coerce')

# Fonction de diagnostic pour les datasets
def diagnostic(df, name):
    print("\n--- Diagnostic " + name + " ---")
    print("Types de variables :\n", df.dtypes)  # types de chaque variable
    print("\nValeurs manquantes :\n", df.isna().sum())  # compte des valeurs manquantes
    print("\nDoublons :", df.duplicated().sum())  # compte des doublons

# Application du diagnostic
diagnostic(data1, "Data_1")
diagnostic(data2, "Data_2")

# Nettoyage des doublons
data1 = data1.drop_duplicates() 
data2 = data2.drop_duplicates()

# Fusion des deux tables sur la colonne 'identifiant'
merged = data1.merge(data2, on="identifiant", how="inner")

# Affichage des dimensions
print("\nDimensions Data_1 :", data1.shape)
print("\nDimensions Data_2 :", data2.shape)
print("\nDimensions après fusion :", merged.shape) 

# Préparation des données

# Dictionnaire pour renommer les colonnes
rename_map = {
    "AGE": "age",
    "Age": "age",
    "SEXE": "sexe",
    "Sexe": "sexe",
    "MARCHE": "marche",
    "Marché": "marche",
    "CSP": "csp",
    "SECTEUR": "secteur",
    "Secteur": "secteur",
    "TYPE_CLIENT": "type_client",
    "Type_client": "type_client",
    "TYPE CLIENT": "type_client",
    "SITUATION_FAMILLE": "situation_famille",
    "Situation_famille": "situation_famille",
    "EPARGNE_FINANCIERE": "epargne_financiere",
    "Epargne_financiere": "epargne_financiere",
    "EPARGNE_TOTAL": "epargne_total",
    "Epargne_total": "epargne_total"
}
merged = merged.rename(columns=rename_map)
merged = merged.drop_duplicates()

# Sélection des colonnes nécessaires
needed = ["age", "marche", "csp", "type_client", "situation_famille", "epargne_financiere", "epargne_total"]
available = [c for c in needed if c in merged.columns]
merged = merged[available].copy()

# Conversion en numérique des colonnes numériques
for col in ["age", "epargne_financiere", "epargne_total"]:
    if col in merged.columns:
        merged[col] = pd.to_numeric(merged[col], errors="coerce")

# Suppression des lignes sans valeurs cibles
merged = merged.dropna(subset=["age", "epargne_total", "epargne_financiere"])

# -----------------------------
# GRAPHIQUES
# -----------------------------

# 1) Histogramme par tranche d’âge (Épargne totale)
bins = [18, 30, 40, 50, 60, 70, 80, 100]
labels = ["18-29", "30-39", "40-49", "50-59", "60-69", "70-79", "80+"]
merged["tranche_age"] = pd.cut(merged["age"], bins=bins, labels=labels, right=False)

# Calcul de l'épargne totale moyenne par tranche d’âge
epargne_par_tranche_total = merged.groupby("tranche_age")["epargne_total"].mean()

# Affichage histogramme
plt.figure(figsize=(8,5))
plt.bar(epargne_par_tranche_total.index.astype(str), epargne_par_tranche_total.values, color="#2C7FB8", alpha=0.7)
plt.xlabel("Tranches d’âge")
plt.ylabel("Épargne totale moyenne")
plt.title("Épargne totale moyenne par tranche d’âge")
plt.grid(axis="y", alpha=0.3)
plt.show()


# 2) Camembert CSP (Épargne totale)

# Somme de l'épargne totale par CSP
epargne_par_csp_total = merged.groupby("csp")["epargne_total"].sum().sort_values(ascending=False)

top_n = 5
top_csp_total = epargne_par_csp_total[:top_n]
other_csp_total = pd.Series({"Autres": epargne_par_csp_total[top_n:].sum()})
epargne_csp_total_final = pd.concat([top_csp_total, other_csp_total])

colors_total = plt.cm.Set3.colors[:len(epargne_csp_total_final)]

# Affichage du camembert
plt.figure(figsize=(7,7))
plt.pie(
    epargne_csp_total_final,
    labels=None,
    autopct="%1.1f%%",
    startangle=90,
    colors=colors_total
)
plt.title("Répartition de l’épargne totale par CSP")
plt.legend(epargne_csp_total_final.index, loc="center left", bbox_to_anchor=(1, 0.5))
plt.tight_layout()
plt.show()


# 3) Histogramme par tranche d’âge (Épargne financière)

# Calcul de l'épargne financière moyenne par tranche d’âge
epargne_par_tranche_fin = merged.groupby("tranche_age")["epargne_financiere"].mean()

# Affichage histogramme
plt.figure(figsize=(8,5))
plt.bar(epargne_par_tranche_fin.index.astype(str), epargne_par_tranche_fin.values, color="#FF7F0E", alpha=0.7)
plt.xlabel("Tranches d’âge")
plt.ylabel("Épargne financière moyenne")
plt.title("Épargne financière moyenne par tranche d’âge")
plt.grid(axis="y", alpha=0.3)
plt.show()


# 4) Camembert CSP (Épargne financière)

# Somme de l'épargne financière par CSP
epargne_par_csp_fin = merged.groupby("csp")["epargne_financiere"].sum().sort_values(ascending=False)

top_csp_fin = epargne_par_csp_fin[:top_n]
other_csp_fin = pd.Series({"Autres": epargne_par_csp_fin[top_n:].sum()})
epargne_csp_fin_final = pd.concat([top_csp_fin, other_csp_fin])

colors_fin = plt.cm.Pastel1.colors[:len(epargne_csp_fin_final)]

# Affichage du camembert
plt.figure(figsize=(7,7))
plt.pie(
    epargne_csp_fin_final,
    labels=None,
    autopct="%1.1f%%",
    startangle=90,
    colors=colors_fin
)
plt.title("Répartition de l’épargne financière par CSP")
plt.legend(epargne_csp_fin_final.index, loc="center left", bbox_to_anchor=(1, 0.5))
plt.tight_layout()
plt.show()


# 5) Histogrammes des tranches d'épargne par catégorie

# Définir les tranches d'épargne selon les quartiles
merged["tranche_epargne_total"] = pd.qcut(merged["epargne_total"], q=4, labels=["Faible", "Moyenne", "Haute", "Très haute"])

# Fonction pour tracer les histogrammes
def plot_histogram(df, cat_col, tranche_col, title, colors=None):
    count_data = df.groupby([cat_col, tranche_col]).size().unstack(fill_value=0)
    count_data = count_data[["Faible", "Moyenne", "Haute", "Très haute"]]  # ordre cohérent
    count_data.plot(kind="bar", figsize=(8,5), color=colors)
    plt.xlabel(cat_col.capitalize())
    plt.ylabel("Nombre de personnes")
    plt.title(title)
    plt.legend(title=tranche_col)
    plt.tight_layout()
    plt.show()

# Couleurs pour les tranches
hist_colors = ["#d73027","#fc8d59","#fee090","#91bfdb"]

# Histogramme selon marché
plot_histogram(merged, "marche", "tranche_epargne_total", "Distribution des tranches d'épargne totale par marché", hist_colors)

# Histogramme selon type de client
plot_histogram(merged, "type_client", "tranche_epargne_total", "Distribution des tranches d'épargne totale par type de client", hist_colors)
