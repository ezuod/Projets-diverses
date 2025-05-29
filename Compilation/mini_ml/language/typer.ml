open Type_system
open Ast
open Typer_util

let rec type_expr (counter : Counter.t) (env : type_lang Util.Environment.t)
  (expr : expr) : type_lang * (type_lang * type_lang) list =
match expr with
| Cst_i (_, ann) ->
    Annotation.set_type ann TInt;
    (TInt, [])
| Cst_b (_, ann) ->
    Annotation.set_type ann TBool;
    (TBool, [])
| Cst_str (_, ann) ->
    Annotation.set_type ann TString;
    (TString, [])
| Cst_func (bu, ann) ->
    let ty = type_of_built_in bu in
    Annotation.set_type ann ty;
    (ty, [])
| Nil ann ->
    let a = TUniv (Counter.get_fresh counter) in
    let ty = TList ([], a) in
    Annotation.set_type ann ty;
    (ty, [])
| Unit ann ->
    Annotation.set_type ann TUnit;
    (TUnit, [])
| Var (x, ann) -> (
    match Util.Environment.get env x with
    | Some ty ->
        let inst_ty = instantiate counter ty in
        Annotation.set_type ann inst_ty;
        (inst_ty, [])
    | None -> failwith ("type_expr: variable not initialized"))
| Fun (arg, body, ann) ->
    let a = TUniv (Counter.get_fresh counter) in
    let env' = Util.Environment.add env arg a in
    let ret_ty, cs = type_expr counter env' body in
    let fn_ty = TFunc ([], a, ret_ty) in
    Annotation.set_type ann fn_ty;
    (fn_ty, cs)
| App (f_e, arg_e, ann) ->
    let f_ty, c1 = type_expr counter env f_e in
    let arg_ty, c2 = type_expr counter env arg_e in
    let f_ty_inst = instantiate counter f_ty in
    let res_ty = TUniv (Counter.get_fresh counter) in
    let c_app = [ (f_ty_inst, TFunc ([], arg_ty, res_ty)) ] in
    Annotation.set_type ann res_ty;
    (res_ty, c1 @ c2 @ c_app)
| IfThenElse (c, t, e, ann) ->
    let c_ty, c1 = type_expr counter env c in
    let t_ty, c2 = type_expr counter env t in
    let e_ty, c3 = type_expr counter env e in
    let c_if = [ (c_ty, TBool); (t_ty, e_ty) ] in
    Annotation.set_type ann t_ty;
    (t_ty, c1 @ c2 @ c3 @ c_if)
| Ignore (e1, e2, ann) ->
    let _, c1 = type_expr counter env e1 in
    let e2_ty, c2 = type_expr counter env e2 in
    Annotation.set_type ann e2_ty;
    (e2_ty, c1 @ c2)
| Let (is_rec, name, e1, e2, ann) ->
    let cpt_before = !(counter) in
    let a = TUniv (Counter.get_fresh counter) in
    let env' = if is_rec then Util.Environment.add env name a else env in
    let ty1, c1 = type_expr counter env' e1 in
    let all_constraints = (a, ty1) :: c1 in
    let internal_cs, external_cs = Type_system.split_constraints cpt_before all_constraints in
    let subst = solve_constraints internal_cs in
    let ty1_subst = Type_system.substitute_type_list subst ty1 in
    let env_subst = Util.Environment.map (Type_system.substitute_type_list subst) env in
    let gen_ty = Type_system.generalize cpt_before ty1_subst in
    let env2 = Util.Environment.add env_subst name gen_ty in
    let ty2, c2 = type_expr counter env2 e2 in
    Annotation.set_type ann ty2;
    (ty2, external_cs @ c2)
