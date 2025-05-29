%{
    open Ast
%}

/* Déclarations de précédence et d'associativité */
/* Ces directives définissent l'ordre des opérations et comment les opérateurs sont groupés. */
%nonassoc IN ELSE ARROW          /* mots-clés structurants */
%left SEMICOLON                  /* séquence d'expressions (faible priorité) */
%left OR AND                     /* booléens: ||, && (associatifs à gauche) */
%left EQ NEQ LT LEQ GT GEQ       /* comparateurs: =, <>, <, <=, >, >= (associatifs à gauche) */
%left CONCAT                     /* concaténation de chaînes: ^ (associatif à gauche) */
%right CAT                       /* opérateurs de listes: :: (associatif à droite) */
%left ADD SUB APPEND             /* +, -, @ (additifs, associatifs à gauche) */
%left MUL DIV MOD                /* *, /, mod/% (multiplicatifs, plus prioritaires) */
%right NEG                       /* opérateurs unaires: neg (plus prioritaire, non associatif) */

%start <Ast.t> main

%%

main:
| l = req_list EOF { l }

req_list:
| r = req l = req_list { r :: l }
| r = req { [r] }

req:
| d = fun_def { let (is_rec, name, e) = d in (is_rec, name, e) }

fun_def:
| LET name = ID args = many_args EQ e = expr {
    let fun_expr = List.fold_right (fun arg acc -> Fun(arg, acc, Annotation.create $loc)) args e in
    (false, name, fun_expr) }
| LET REC name = ID args = many_args EQ e = expr {
    let fun_expr = List.fold_right (fun arg acc -> Fun(arg, acc, Annotation.create $loc)) args e in
    (true, name, fun_expr) }

many_args:
| { [] }
| id = ID args = many_args { id :: args }

expr:
| e = simple_expr { e }
| IF test = expr THEN th = expr ELSE el = expr { IfThenElse(test, th, el, Annotation.create $loc) }
| d = fun_def IN e2 = expr {
    let (is_rec, name, e1) = d in
    Let(is_rec, name, e1, e2, Annotation.create $loc) }
| FUN x = ID ARROW e = expr { Fun(x, e, Annotation.create $loc) }
| e1 = expr SEMICOLON e2 = expr { Ignore(e1, e2, Annotation.create $loc) }
| SUB e = expr %prec NEG { App(Cst_func(UMin, Annotation.create $loc), e, Annotation.create $loc) }
| e1 = expr op = binop e2 = expr {
    App(App(Cst_func(op, Annotation.create $loc), e1, Annotation.create $loc), e2, Annotation.create $loc) }
| e1 = app_expr e2 = simple_expr { App(e1, e2, Annotation.create $loc) }

simple_expr:
| i = INT { Cst_i(i, Annotation.create $loc) }
| b = BOOL { Cst_b(b, Annotation.create $loc) }
| s = STRING { Cst_str(s, Annotation.create $loc) }
| f = built_in { Cst_func(f, Annotation.create $loc) }
| L_PAR R_PAR { Unit(Annotation.create $loc) }
| L_SQ R_SQ { Nil(Annotation.create $loc) } /* Liste vide */
| L_SQ l = separated_nonempty_list(SEMICOLON, simple_expr) R_SQ {
    let loc = Annotation.create $loc in
    List.fold_right (fun elt acc -> App(App(Cst_func(Cat, loc), elt, loc), acc, loc)) l (Nil loc) }
| x = ID { Var(x, Annotation.create $loc) }
| L_PAR e = expr R_PAR { e }

app_expr:
| f = simple_expr { f }
| f = app_expr e = simple_expr { App(f, e, Annotation.create $loc) }

%inline binop:
| ADD   { Add }
| SUB   { Sub }
| MUL   { Mul }
| DIV   { Div }
| MOD   { Mod }
| AND   { And }
| OR    { Or }
| EQ    { Eq }
| NEQ   { Neq }
| LT    { Lt }
| GT    { Gt }
| LEQ   { Leq }
| GEQ   { Geq }
| CONCAT { Concat }
| CAT   { Cat }
| APPEND { Append }

%inline built_in:
| L_PAR b = binop R_PAR { b }
| NEG   { UMin }
| NOT   { Not }
| HEAD  { Head }
| TAIL  { Tail }
| PRINT { Print }