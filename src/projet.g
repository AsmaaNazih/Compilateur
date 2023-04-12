// Grammaire du langage PROJET
// CMPL L3info 
// Nathalie Girard, Veronique Masson, Laurent Perraudeau
// il convient d'y inserer les appels a {PtGen.pt(k);}
// relancer Antlr apres chaque modification et raffraichir le projet Eclipse le cas echeant

// attention l'analyse est poursuivie apres erreur si l'on supprime la clause rulecatch

grammar projet;

options {
  language=Java; k=1;
 }

@header {           
import java.io.IOException;
import java.io.DataInputStream;
import java.io.FileInputStream;
} 


// partie syntaxique :  description de la grammaire //
// les non-terminaux doivent commencer par une minuscule


@members {

 
// variables globales et methodes utiles a placer ici
  
}
// la directive rulecatch permet d'interrompre l'analyse a la premiere erreur de syntaxe
@rulecatch {
catch (RecognitionException e) {reportError (e) ; throw e ; }}


unite  :   unitprog  EOF
      |    unitmodule  EOF
  ;
  
unitprog
  : 'programme' ident  {PtGen.pt(54);} ':'  
     declarations  
    {PtGen.pt(48);} corps { System.out.println("succes, arret de la compilation "); } { PtGen.pt(255); } {PtGen.pt(252);}
  ;
  
unitmodule
  : 'module' ident {PtGen.pt(60);} ':' 
     declarations   {PtGen.pt(252);} { PtGen.pt(253); } 
  ;
  
declarations
  : partiedef? partieref? consts? vars? decprocs? 
  ;
  
partiedef
  : 'def' ident  {PtGen.pt(55);} (',' ident  {PtGen.pt(55);})* ptvg
  ;
  
partieref: 'ref'  specif   (',' specif )* ptvg {PtGen.pt(59);}
  ;
  
specif  : ident {PtGen.pt(56);} {PtGen.pt(58);} ( 'fixe' '(' type {PtGen.pt(57);} ( ',' type {PtGen.pt(57);} )* ')' )? 
                 ( 'mod'  '(' type {PtGen.pt(57);} ( ',' type {PtGen.pt(57);} )* ')' )? 
  ;
  
consts  : 'const' ( ident  '=' valeur  ptvg { PtGen.pt(29); } { PtGen.pt(31); } )+ 
  ;
  
vars  : 'var' ( type ident { PtGen.pt(30); }  ( ','  ident {PtGen.pt(29);} { PtGen.pt(30); }   )* ptvg  )+ {PtGen.pt(32);} 
  ;
  
type  : 'ent'  {PtGen.pt(27);}
  |     'bool' {PtGen.pt(26);}
  ;
  
decprocs: {PtGen.pt(44);} (decproc ptvg)+
  ;
  
decproc :  'proc'  ident { PtGen.pt(42); } parfixe? parmod? { PtGen.pt(43); }consts? vars? corps 
  ;
  
ptvg  : ';'
  | 
  ;
  
corps : 'debut' instructions  'fin' { PtGen.pt(254); } 
  ;
  
parfixe: 'fixe' '(' pf  ( ';' pf )* ')'
  ;
  
pf  : type ident { PtGen.pt(45); } ( ',' ident {PtGen.pt(45);} )*  
  ;

parmod  : 'mod' '(' pm  ( ';' pm )* ')'
  ;
  
pm  : type ident { PtGen.pt(46); } ( ',' ident {PtGen.pt(46);} )*
  ;
  
instructions
  : instruction ( ';' instruction)*
  ;
  
instruction
  : inssi
  | inscond
  | boucle
  | lecture
  | ecriture
  | affouappel 
  |
  ;
  
inssi : 'si' expression {PtGen.pt(34);}  'alors' instructions ('sinon' { PtGen.pt(36);} instructions)? 'fsi' {PtGen.pt(33);}
  ;
  
inscond : 'cond' {PtGen.pt(39); } expression {PtGen.pt(34); } ':' instructions 
          (',' {PtGen.pt(40); } expression {PtGen.pt(34); } ':' instructions)* 
          ('aut' {PtGen.pt(40); } instructions |  ) 
          'fcond' {PtGen.pt(41); }
  ;
  
boucle  : 'ttq' {PtGen.pt(37);} expression {PtGen.pt(34);} 'faire' instructions 'fait' {PtGen.pt(38);} {PtGen.pt(35);} 
  ;
  
lecture: 'lire' '(' ident { PtGen.pt(47); }{ PtGen.pt(25); } ( ',' ident { PtGen.pt(47); } { PtGen.pt(25); } )* ')' 
  ;
  
ecriture: 'ecrire' '(' expression { PtGen.pt(20); } ( ',' expression { PtGen.pt(20); } )* ')'
   ;
  
affouappel
  : ident  (    ':=' {PtGen.pt(21);} { PtGen.pt(24); } expression { PtGen.pt(62); }  { PtGen.pt(25); } 
            |  {PtGen.pt(49);} (effixes (effmods)?)?  {PtGen.pt(52);}
           )
  ;
   
effixes : '(' (expression {PtGen.pt(53);} (',' expression {PtGen.pt(53);} )*)? ')' {PtGen.pt(51);}
  ;
  
effmods :'(' (ident {PtGen.pt(50);}  (',' ident {PtGen.pt(50);} )*)? ')' 
  ; 
  
expression: (exp1) ('ou' { PtGen.pt(6); } exp1  { PtGen.pt(6); } { PtGen.pt(10); } )*
  ;
  
exp1  : exp2 ('et' { PtGen.pt(6); } exp2 { PtGen.pt(6); } { PtGen.pt(9); } )*
  ;
  
exp2  : 'non' exp2 { PtGen.pt(6); }  { PtGen.pt(11); }
  | exp3  
  ;
  
exp3  : exp4 
  ( '=' { PtGen.pt(2); }  exp4 { PtGen.pt(2); } { PtGen.pt(5); }
  | '<>'{ PtGen.pt(2); }  exp4 { PtGen.pt(2); } { PtGen.pt(12); }
  | '>' { PtGen.pt(2); }  exp4 { PtGen.pt(2); } { PtGen.pt(13); }
  | '>='{ PtGen.pt(2); }  exp4 { PtGen.pt(2); } { PtGen.pt(14); }
  | '<' { PtGen.pt(2); }  exp4 { PtGen.pt(2); } { PtGen.pt(15); }
  | '<='{ PtGen.pt(2); }  exp4 { PtGen.pt(2); } { PtGen.pt(16); }
  ) ?
  ;
  
exp4  : exp5 
        ('+'  { PtGen.pt(2); } exp5 { PtGen.pt(2); } { PtGen.pt(8); }
        |'-'  { PtGen.pt(2); } exp5 { PtGen.pt(2); } { PtGen.pt(17); }
        )*    
  ;
  
exp5  : primaire 
        (    '*' { PtGen.pt(2); }  primaire { PtGen.pt(2); } { PtGen.pt(18); }
          | 'div' { PtGen.pt(2); } primaire { PtGen.pt(2); } { PtGen.pt(19); }
        )*
  ;
  
primaire: valeur { PtGen.pt(4); }
  | ident  { PtGen.pt(21); } { PtGen.pt(1); }
  | '(' expression ')'
  ;
  
valeur  : nbentier { PtGen.pt(7); }
  | '+' nbentier { PtGen.pt(7); }
  | '-' nbentier { PtGen.pt(3); }
  | 'vrai' { PtGen.pt(22); }
  | 'faux' { PtGen.pt(23); }
  ;

// partie lexicale  : cette partie ne doit pas etre modifiee  //
// les unites lexicales de ANTLR doivent commencer par une majuscule
// Attention : ANTLR n'autorise pas certains traitements sur les unites lexicales, 
// il est alors ncessaire de passer par un non-terminal intermediaire 
// exemple : pour l'unit lexicale INT, le non-terminal nbentier a du etre introduit
 
      
nbentier  :   INT { UtilLex.valEnt = Integer.parseInt($INT.text);}; // mise a jour de valEnt

ident : ID  { UtilLex.traiterId($ID.text); } ; // mise a jour de numIdCourant
     // tous les identificateurs seront places dans la table des identificateurs, y compris le nom du programme ou module
     // (NB: la table des symboles n'est pas geree au niveau lexical mais au niveau du compilateur)
        
  
ID  :   ('a'..'z'|'A'..'Z')('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ; 
     
// zone purement lexicale //

INT :   '0'..'9'+ ;
WS  :   (' '|'\t' |'\r')+ {skip();} ; // definition des "blocs d'espaces"
RC  :   ('\n') {UtilLex.incrementeLigne(); skip() ;} ; // definition d'un unique "passage a la ligne" et comptage des numeros de lignes

COMMENT
  :  '\{' (.)* '\}' {skip();}   // toute suite de caracteres entouree d'accolades est un commentaire
  |  '#' ~( '\r' | '\n' )* {skip();}  // tout ce qui suit un caractere diese sur une ligne est un commentaire
  ;

// commentaires sur plusieurs lignes
ML_COMMENT    :   '/*' (options {greedy=false;} : .)* '*/' {$channel=HIDDEN;}
    ;	   



	   
