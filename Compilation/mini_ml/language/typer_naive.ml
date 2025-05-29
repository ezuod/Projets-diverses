open Ast
open Typer_util

let rec type_expr (counter : Counter.t) (env : type_lang Util.Environment.t)
    (expr : expr) =
  (* la suite est à modifier -- c’est juste là pour ne pas avoir de warning tant que vous ne travaillez pas dessus.*)
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
          Annotation.set_type ann ty;
          (ty, [])
      | None -> failwith ("type_expr: variable not initialized"))
  | Fun (arg, body, ann) ->
      let a = TUniv (Counter.get_fresh counter) in
      let _ = Util.Environment.add env arg a in
      let ret_ty, cs = type_expr counter env body in
      let fn_ty = TFunc ([], a, ret_ty) in
      Annotation.set_type ann fn_ty;
      (fn_ty, cs)
  | App (f_e, arg_e, ann) ->
      let f_ty, c1 = type_expr counter env f_e in
      let arg_ty, c2 = type_expr counter env arg_e in
      let res = TUniv (Counter.get_fresh counter) in
      let c_app = [ (f_ty, TFunc ([], arg_ty, res)) ] in
      Annotation.set_type ann res;
      (res, c1 @ c2 @ c_app)
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
      let a = TUniv (Counter.get_fresh counter) in
      let _ = if is_rec then Util.Environment.add env name a in
      let e1_ty, c1 = type_expr counter env e1 in
      let c_bind = [ (a, e1_ty) ] in
      let _ = Util.Environment.add env name a in
      let e2_ty, c2 = type_expr counter env e2 in
      Annotation.set_type ann e2_ty;
      (e2_ty, c1 @ c_bind @ c2)