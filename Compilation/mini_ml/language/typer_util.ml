open Type_system
open Ast

exception Constraint_error of type_lang * type_lang
exception Typing_error of Util.Position.t * string

module Counter = struct
  type t = int ref

  let create () = ref 0

  let get_fresh counter =
    let res = !counter in
    counter := !counter + 1;
    res
end

let type_of_built_in (built_in : built_in) =
  match built_in with
  | Add | Sub | Mul | Div | Mod -> TFunc ([], TInt, TFunc ([], TInt, TInt))
  | And | Or -> TFunc ([], TBool, TFunc ([], TBool, TBool))
  | Eq | Neq | Lt | Leq | Gt | Geq ->
      let a' = TUniv 0 in
      TFunc ([ 0 ], a', TFunc ([], a', TBool))
  | Concat -> TFunc ([], TString, TFunc ([], TString, TString))
  | Cat ->
      let a' = TUniv 0 in
      TFunc ([ 0 ], a', TFunc ([], TList ([], a'), TList ([], a')))
  | Append ->
      let a' = TUniv 0 in
      TFunc ([ 0 ], TList ([], a'), TFunc ([], TList ([], a'), TList ([], a')))
  | UMin -> TFunc ([], TInt, TInt)
  | Not -> TFunc ([], TBool, TBool)
  | Head ->
      let a' = TUniv 0 in
      TFunc ([ 0 ], TList ([], a'), a')
  | Tail ->
      let a' = TUniv 0 in
      TFunc ([ 0 ], TList ([], a'), TList ([], a'))
  | Print ->
      let a' = TUniv 0 in
      TFunc ([ 0 ], a', TUnit)

let rec solve_constraints (constraints : (type_lang * type_lang) list) :
    (int * type_lang) list =
  match constraints with
  | [] -> []
  | ((TFunc _ as f), TBool) :: _ -> raise (Constraint_error (f, TBool))
  | (TUniv n, TUniv m) :: r when not (n = m) ->
      let minim, maxim = if n < m then (n, m) else (m, n) in
      let s = (maxim, TUniv minim) in
      let r' = List.map (substitute_constraint maxim (TUniv minim)) r in
      s :: solve_constraints r'
  | ((TUniv n as u), t2) :: r ->
      if u = t2 then solve_constraints r
      else if List.mem n (get_free_type_var t2) then
        raise (Constraint_error (u, t2))
      else
        let r' = List.map (substitute_constraint n t2) r in
        (n, t2) :: solve_constraints r'
  | (t1, TUniv m) :: r -> solve_constraints ((TUniv m, t1) :: r)
  | (TList (_, a1), TList (_, a2)) :: r -> solve_constraints ((a1, a2) :: r)
  | (TFunc (_, a1, r1), TFunc (_, a2, r2)) :: r ->
      solve_constraints ((a1, a2) :: (r1, r2) :: r)
  | (t1, t2) :: r when t1 = t2 -> solve_constraints r
  | (t1, t2) :: _ -> raise (Constraint_error (t1, t2))

let instantiate (counter : Counter.t) (type_lang : type_lang) : type_lang =
  let rec apply_subst subst t =
    match t with
    | TUniv n -> (
        try List.assoc n subst with Not_found -> t
      )
    | TInt | TBool | TString | TUnit -> t
    | TList (_, t1) -> TList ([], apply_subst subst t1)
    | TFunc (_, t1, t2) -> TFunc ([], apply_subst subst t1, apply_subst subst t2)
  in
  match type_lang with
  | TFunc (gens, t1, t2) ->
      let subst = List.map (fun id -> (id, TUniv (Counter.get_fresh counter))) gens in
      TFunc ([], apply_subst subst t1, apply_subst subst t2)
  | TList (gens, t1) ->
      let subst = List.map (fun id -> (id, TUniv (Counter.get_fresh counter))) gens in
      TList ([], apply_subst subst t1)
  | _ -> type_lang  (* types non polymorphes : pas de changement *)