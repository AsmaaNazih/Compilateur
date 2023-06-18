/*********************************************************************************
 * VARIABLES ET METHODES FOURNIES PAR LA CLASSE UtilLex (cf libClass_Projet)     *
 *       complement à l'ANALYSEUR LEXICAL produit par ANTLR                      *
 *                                                                               *
 *                                                                               *
 *   nom du programme compile, sans suffixe : String UtilLex.nomSource           *
 *   ------------------------                                                    *
 *                                                                               *
 *   attributs lexicaux (selon items figurant dans la grammaire):                *
 *   ------------------                                                          *
 *     int UtilLex.valEnt = valeur du dernier nombre entier lu (item nbentier)   *
 *     int UtilLex.numIdCourant = code du dernier identificateur lu (item ident) *
 *                                                                               *
 *                                                                               *
 *   methodes utiles :                                                           *
 *   ---------------                                                             *
 *     void UtilLex.messErr(String m)  affichage de m et arret compilation       *
 *     String UtilLex.chaineIdent(int numId) delivre l'ident de codage numId     *
 *     void afftabSymb()  affiche la table des symboles                          *
 *********************************************************************************/


import java.io.*;

/**
 * classe de mise en oeuvre du compilateur
 * =======================================
 * (verifications semantiques + production du code objet)
 * 
 * @author Girard, Masson, Perraudeau
 *
 */

public class PtGen {
    

    // constantes manipulees par le compilateur
    // ----------------------------------------

	private static final int 
	
	// taille max de la table des symboles
	MAXSYMB=300,

	// codes MAPILE :
	RESERVER=1,EMPILER=2,CONTENUG=3,AFFECTERG=4,OU=5,ET=6,NON=7,INF=8,
	INFEG=9,SUP=10,SUPEG=11,EG=12,DIFF=13,ADD=14,SOUS=15,MUL=16,DIV=17,
	BSIFAUX=18,BINCOND=19,LIRENT=20,LIREBOOL=21,ECRENT=22,ECRBOOL=23,
	ARRET=24,EMPILERADG=25,EMPILERADL=26,CONTENUL=27,AFFECTERL=28,
	APPEL=29,RETOUR=30,

	// codes des valeurs vrai/faux
	VRAI=1, FAUX=0,

    // types permis :
	ENT=1,BOOL=2,NEUTRE=3,

	// categories possibles des identificateurs :
	CONSTANTE=1,VARGLOBALE=2,VARLOCALE=3,PARAMFIXE=4,PARAMMOD=5,PROC=6,
	DEF=7,REF=8,PRIVEE=9,

    //valeurs possible du vecteur de translation 
    TRANSDON=1,TRANSCODE=2,REFEXT=3;


    // utilitaires de controle de type
    // -------------------------------
    /**
     * verification du type entier de l'expression en cours de compilation 
     * (arret de la compilation sinon)
     */
	private static void verifEnt() {
		if (tCour != ENT)
			UtilLex.messErr("expression entiere attendue");
	}
	/**
	 * verification du type booleen de l'expression en cours de compilation 
	 * (arret de la compilation sinon)
	 */
	private static void verifBool() {
		if (tCour != BOOL)
			UtilLex.messErr("expression booleenne attendue");
	}

    // pile pour gerer les chaines de reprise et les branchements en avant
    // -------------------------------------------------------------------

    private static TPileRep pileRep;  


    // production du code objet en memoire
    // -----------------------------------

    private static ProgObjet po;
    
    
    // COMPILATION SEPAREE 
    // -------------------
    //
    /** 
     * modification du vecteur de translation associe au code produit 
     * + incrementation attribut nbTransExt du descripteur
     *  NB: effectue uniquement si c'est une reference externe ou si on compile un module
     * @param valeur : TRANSDON, TRANSCODE ou REFEXT
     */
    private static void modifVecteurTrans(int valeur) {
		if (valeur == REFEXT || desc.getUnite().equals("module")) {
			po.vecteurTrans(valeur);
			desc.incrNbTansExt();
		}
	}    
    // descripteur associe a un programme objet (compilation separee)
    private static Descripteur desc;

     
    // autres variables fournies
    // -------------------------
    
 // MERCI de renseigner ici un nom pour le trinome, constitue EXCLUSIVEMENT DE LETTRES
    public static String trinome="HermenierNazihZidane"; 
    
    private static int tCour; // type de l'expression compilee
    private static int vCour; // sert uniquement lors de la compilation d'une valeur (entiere ou boolenne)
  
   
    // TABLE DES SYMBOLES
    // ------------------
    //
    private static EltTabSymb[] tabSymb = new EltTabSymb[MAXSYMB + 1];
    
    // it = indice de remplissage de tabSymb
    // bc = bloc courant (=1 si le bloc courant est le programme principal)
	private static int it, bc;
	private static int ad;
	private static int indAff;
	private static int cptParam;
	private static int refAdresseProc;
	private static int nbrParam;
	private static boolean hasProc;
	private static int tailleGlobaux;
	//private static int chainage;
	
	/** 
	 * utilitaire de recherche de l'ident courant (ayant pour code UtilLex.numIdCourant) dans tabSymb
	 * 
	 * @param borneInf : recherche de l'indice it vers borneInf (=1 si recherche dans tout tabSymb)
	 * @return : indice de l'ident courant (de code UtilLex.numIdCourant) dans tabSymb (O si absence)
	 */
	private static int presentIdent(int borneInf) {
		int i = it;
		while (i >= borneInf && tabSymb[i].code != UtilLex.numIdCourant)
			i--;
		if (i >= borneInf)
			return i;
		else
			return 0;
	}

	/**
	 * utilitaire de placement des caracteristiques d'un nouvel ident dans tabSymb
	 * 
	 * @param code : UtilLex.numIdCourant de l'ident
	 * @param cat : categorie de l'ident parmi CONSTANTE, VARGLOBALE, PROC, etc.
	 * @param type : ENT, BOOL ou NEUTRE
	 * @param info : valeur pour une constante, ad d'exécution pour une variable, etc.
	 */
	private static void placeIdent(int code, int cat, int type, int info) {
		if (it == MAXSYMB)
			UtilLex.messErr("debordement de la table des symboles");
		it = it + 1;
		tabSymb[it] = new EltTabSymb(code, cat, type, info);
	}

	/**
	 *  utilitaire d'affichage de la table des symboles
	 */
	private static void afftabSymb() { 
		System.out.println("       code           categorie      type    info");
		System.out.println("      |--------------|--------------|-------|----");
		for (int i = 1; i <= it; i++) {
			if (i == bc) {
				System.out.print("bc=");
				Ecriture.ecrireInt(i, 3);
			} else if (i == it) {
				System.out.print("it=");
				Ecriture.ecrireInt(i, 3);
			} else
				Ecriture.ecrireInt(i, 6);
			if (tabSymb[i] == null)
				System.out.println(" reference NULL");
			else
				System.out.println(" " + tabSymb[i]);
		}
		System.out.println();
	}
    

	/**
	 *  initialisations A COMPLETER SI BESOIN
	 *  -------------------------------------
	 */
	public static void initialisations() {
	
		// indices de gestion de la table des symboles
		it = 0;
		bc = 1;
		ad=0;
		indAff = -1; //indice d'une variable pour l'affectation
		cptParam = 0;
		refAdresseProc = 0;
		nbrParam = 0;
		hasProc = false;
		tailleGlobaux = 0;
		// pile des reprises pour compilation des branchements en avant
		pileRep = new TPileRep(); 
		// programme objet = code Mapile de l'unite en cours de compilation
		po = new ProgObjet();
		// COMPILATION SEPAREE: desripteur de l'unite en cours de compilation
		desc = new Descripteur();
		
		// initialisation necessaire aux attributs lexicaux
		UtilLex.initialisation();
	
		// initialisation du type de l'expression courante
		tCour = NEUTRE;

	} // initialisations

	/**
	 *  code des points de generation A COMPLETER
	 *  -----------------------------------------
	 * @param numGen : numero du point de generation a executer
	 */
	public static void pt(int numGen) {
	
		switch (numGen) {
		case 0:
			initialisations();
			break;
			//valeur
		case 1: int ind = presentIdent(1); 
				if(ind != 0){
					switch(tabSymb[ind].categorie){
						case CONSTANTE: po.produire(EMPILER);
										po.produire(tabSymb[ind].info);
										break;
						case VARGLOBALE:po.produire(CONTENUG);
										po.produire(tabSymb[ind].info);
										modifVecteurTrans(TRANSDON);
										break;
						case PARAMFIXE , VARLOCALE : po.produire(CONTENUL); 
													 po.produire(tabSymb[ind].info);
													 po.produire(0);
													 break;
						case PARAMMOD : po.produire(CONTENUL); 
										po.produire(tabSymb[ind].info);
										po.produire(1); break;
					}}
				tCour = tabSymb[ind].type;
				break;
		case 2: verifEnt();break;
		case 3: tCour = ENT; vCour = UtilLex.valEnt * -1; break;
		case 4: po.produire(EMPILER);po.produire(vCour); break;
		case 5: tCour = BOOL; po.produire(EG); break;
		case 6: verifBool(); break;
		case 7: tCour = ENT; vCour = UtilLex.valEnt; break;
		case 8: tCour = ENT; po.produire(ADD); break;
		case 9: tCour = BOOL; po.produire(ET); break;
		case 10: tCour = BOOL; po.produire(OU); break;
		case 11: tCour = BOOL; po.produire(NON);break;
		case 12: tCour = BOOL; po.produire(DIFF); break;
		case 13: tCour = BOOL; po.produire(SUP); break;
		case 14: tCour = BOOL; po.produire(SUPEG); break;
		case 15: tCour = BOOL; po.produire(INF); break;
		case 16: tCour = BOOL; po.produire(INFEG); break;
		case 17: tCour = ENT; po.produire(SOUS);break;
		case 18: tCour = ENT; po.produire(MUL);break;
		case 19: tCour = ENT; po.produire(DIV);break;
		case 20: if(tCour==ENT)po.produire(ECRENT); else po.produire(ECRBOOL);break;
		case 21: if(presentIdent(1)==0) {UtilLex.messErr("variable " + UtilLex.chaineIdent(UtilLex.numIdCourant)+" non déclaré" );UtilLex.arret();} break;
		case 22: tCour = BOOL; vCour = VRAI; break;
		case 23: tCour = BOOL; vCour = FAUX; break;
		case 24: int in = presentIdent(1);
				 if(in != 0){
					 switch(tabSymb[in].categorie) {
					 	case CONSTANTE , PARAMFIXE: UtilLex.messErr("Constante "+ UtilLex.chaineIdent(tabSymb[in].code )+" ne peut être modifié"); 
													UtilLex.arret(); 
													break;
						case VARGLOBALE , VARLOCALE , PARAMMOD: indAff = in;
													break;
						default : UtilLex.messErr("Catégorie identifiant inexistante");
								  UtilLex.arret();
					  }
				  }else {
					  UtilLex.messErr("Identifiant inexistant !");
					  UtilLex.arret();
				  }break;
		case  25:
			switch(tabSymb[indAff].categorie) {
				case VARLOCALE : po.produire(AFFECTERL); 
					 po.produire(tabSymb[indAff].info); po.produire(0);
					 break;
				case PARAMMOD : po.produire(AFFECTERL); 
				 	po.produire(tabSymb[indAff].info); po.produire(1);
				 	break;
				case VARGLOBALE : po.produire(AFFECTERG); po.produire(tabSymb[indAff].info);
					modifVecteurTrans(TRANSDON);
					break;
			}
			break;
		case 62: if(tCour!=tabSymb[indAff].type) {
					UtilLex.messErr("miss match typage ident expression");
					UtilLex.arret();
				 }
				 break;
		case 26:tCour = BOOL;break;
		case 27: tCour = ENT;break;
		case 28: break;
		case 29: if(presentIdent(bc)!=0) {UtilLex.messErr("nom de variable déjà utilisé : " + UtilLex.chaineIdent(UtilLex.numIdCourant)); UtilLex.arret();} break;
		
			// Var Ident
		case 30: 
			if (bc==1){
				placeIdent(UtilLex.numIdCourant, VARGLOBALE, tCour, ad);
				tailleGlobaux++;
				}	
			else
				{placeIdent(UtilLex.numIdCourant, VARLOCALE,  tCour, cptParam+ad+2);}
			ad++;
			break;
		case 31: placeIdent(UtilLex.numIdCourant, CONSTANTE, tCour, vCour); break;
		case 32: if(desc.getUnite().equals("programme")) {
					 po.produire(RESERVER); 
					 po.produire(ad);
				 }
					 desc.setTailleGlobaux(tailleGlobaux); 
				 break;
		case 33: po.modifier(pileRep.depiler(), po.getIpo()+1);break;
		case 34:
			//expression
			verifBool();
			po.produire(BSIFAUX); 
			po.produire(-1);
			modifVecteurTrans(TRANSCODE);
			pileRep.empiler(po.getIpo());
			break;
		case 35: 
			po.produire(BINCOND);
			po.produire(pileRep.depiler());
			modifVecteurTrans(TRANSCODE);
			break;
		case 36: 
			po.produire(BINCOND);po.produire(-1);
			po.modifier(pileRep.depiler(), po.getIpo()+1);
			pileRep.empiler(po.getIpo());
			modifVecteurTrans(TRANSCODE);
			break;
		case 37: pileRep.empiler(po.getIpo()+1);break;
		case 38: po.modifier(pileRep.depiler(), po.getIpo()+3); break;
		case 39: 
			//cond
			pileRep.empiler(0); break; //marqueur de fin
		case 40:
			//bsifaux precedent
			po.modifier(pileRep.depiler(), po.getIpo()+3);
			//instruction 
			po.produire(BINCOND);
			po.produire(pileRep.depiler());
			modifVecteurTrans(TRANSCODE);
			pileRep.empiler(po.getIpo());
			break;
		case 41:
			//fcond
			int chainage = pileRep.depiler();
			//cas ou aut n'existe pas
			if(po.getElt(chainage)==-1) {
				po.modifier(chainage, po.getIpo()+1);
				chainage = pileRep.depiler();
			}
				
			while(chainage!=0) {
				int nextAd = po.getElt(chainage);
				po.modifier(chainage, po.getIpo()+1);
				chainage = nextAd;
			}
			break;
		case 42:
			   in =presentIdent(bc);
			  if (in==0){ 
			   	  placeIdent(UtilLex.numIdCourant, PROC, NEUTRE, po.getIpo()+1);
			   	  placeIdent(-1, PRIVEE, NEUTRE, 0);
			   	  bc=it+1;
			   	  ad = 0;
			   }else
				   {UtilLex.messErr("Nom de procedure déjà utilisée"); UtilLex.arret();}
			break;
		case 43:
			tabSymb[bc-1].info=cptParam;
			break;
		case 44:
			if(desc.getUnite().equals("programme")) {
			  hasProc = true;
			  po.produire(BINCOND);
		   	  po.produire(-1);
		   	  modifVecteurTrans(TRANSCODE);
		   	  pileRep.empiler(po.getIpo());
			}
			break;
		case 45:
			in = presentIdent(bc);
			if(in==0) {
                placeIdent(UtilLex.numIdCourant, PARAMFIXE , tCour , cptParam);
				cptParam++;
			}else {
				UtilLex.messErr("Nom ParamFixe déjà utilisé");
				UtilLex.arret();
			}
				
			break;
		case 46: 
			in = presentIdent(bc);
			if(in==0) {
                placeIdent(UtilLex.numIdCourant, PARAMMOD , tCour , cptParam);
				cptParam++; 
			}else {
				UtilLex.messErr("Nom ParamFixe déjà utilisé");
				UtilLex.arret();
			}
			break;
		case 47:
			indAff  = presentIdent(bc);
			if(indAff == 0) {
				UtilLex.messErr("variable " + UtilLex.chaineIdent(UtilLex.numIdCourant)+" non déclaré" );
				UtilLex.arret();
				}else {
					if(tabSymb[indAff].type == BOOL)
						po.produire(LIREBOOL);
					else
						po.produire(LIRENT);
				}
			break;
		case 48:
			if(hasProc)
				po.modifier(pileRep.depiler(),po.getIpo() + 1);
			break;
		case 49:
			refAdresseProc = presentIdent(1);
			if(refAdresseProc==0) {
				UtilLex.messErr("Procédure " +  UtilLex.chaineIdent(UtilLex.numIdCourant) + " non déclaré");
				UtilLex.arret();
			}
			if(tabSymb[refAdresseProc].categorie != PROC) {
				UtilLex.messErr( UtilLex.chaineIdent(UtilLex.numIdCourant) + " non déclaré en tant que procédure");
				UtilLex.arret();
			}
			break;
		case 50:
			// check param type incrementer a partir de bc en mm temps
			nbrParam+=1;
			if(tabSymb[refAdresseProc + 1 + nbrParam].type!=tabSymb[presentIdent(1)].type) {
				UtilLex.messErr("Erreur typage paramètre");
				UtilLex.arret();
			}
			
			switch(tabSymb[presentIdent(1)].categorie) {
				case VARGLOBALE : po.produire(EMPILERADG);
								  po.produire(tabSymb[presentIdent(1)].info);
								  modifVecteurTrans(TRANSDON);
								  break;
				case VARLOCALE : po.produire(EMPILERADL);
								 po.produire(tabSymb[presentIdent(1)].info);
								 po.produire(0);
								 break;
				case PARAMMOD : po.produire(EMPILERADL);
				 				 po.produire(tabSymb[presentIdent(1)].info);
				 				 po.produire(1);
				 				 break;
				default : UtilLex.messErr("type passé en paramètre incorrect");
						  UtilLex.arret();
			}
			
			break;
		case 51:
			int i = 2;
			while(i - 2 != nbrParam) {
				if(tabSymb[refAdresseProc + i].categorie!= PARAMFIXE) {
					UtilLex.messErr("Paramètre fixe inattendu !");
					UtilLex.arret();
				}
				i++;
			}
			if(nbrParam !=0 && tabSymb[refAdresseProc + i].categorie==PARAMFIXE) {
				UtilLex.messErr("Paramètre fixe manquant !");
				UtilLex.arret();
			}
			break;
		case 52:
			int nbrP = tabSymb[refAdresseProc + 1].info;
			if(nbrP - nbrParam != 0) {
				UtilLex.messErr("Paramètre mod manquant ou inattendu !");
				UtilLex.arret();
			}
			po.produire(APPEL);
			po.produire(tabSymb[refAdresseProc].info);
			if(tabSymb[refAdresseProc + 1].categorie == REF)
				modifVecteurTrans(REFEXT);
			po.produire(tabSymb[refAdresseProc+1].info);
			nbrParam = 0;
			
			break;
			
		case 53:
			nbrParam+=1;
			if(tabSymb[refAdresseProc + 1 + nbrParam].type==BOOL)
				verifBool();
			if(tabSymb[refAdresseProc + 1 + nbrParam].type==ENT)
				verifEnt();
			break;
			//non fait
			
			//PARTIE COMPILATION SEPAREE/
		case 54: 
			desc.setUnite("programme");
			break;
		case 55:
			String nameDef = UtilLex.chaineIdent(UtilLex.numIdCourant); 
			if(desc.presentDef(nameDef)==0)
				desc.ajoutDef(nameDef);
			else {
				UtilLex.messErr("Def " + nameDef + " déjà déclaré");
				UtilLex.arret();
			}
			break;
		case 56:
			bc=it+1;
			cptParam=0;
			String nameRef = UtilLex.chaineIdent(UtilLex.numIdCourant);
			if(desc.presentRef(nameRef)==0) {
				desc.ajoutRef(nameRef);
				desc.modifRefNbParam(desc.presentRef(nameRef), 0);
			}
			else {
				UtilLex.messErr("Ref " + nameRef + " déjà déclaré");
				UtilLex.arret();
			}
			break;
		case 57:
			int indice = desc.presentRef(UtilLex.chaineIdent(UtilLex.numIdCourant));
			int nbrPara = desc.getRefNbParam(indice);
			desc.modifRefNbParam(indice, nbrPara+1);
			placeIdent(-1, PARAMFIXE, tCour, cptParam++);
			tabSymb[bc+1].info=cptParam;
			break;
		case 58:
			 in =presentIdent(1);
			  if (in==0){ 
			   	  placeIdent(UtilLex.numIdCourant, PROC, NEUTRE, desc.getNbRef());
			   	  placeIdent(-1, REF, NEUTRE, 0);
			   }else
				   {UtilLex.messErr("conflit Ref et nom de procedure"); UtilLex.arret();}
			break;
		case 59:
			bc=1;
			cptParam = 0;
			break;
		case 60: 
			desc.setUnite("module");
			break; 
		case 61:
			indice = desc.presentRef(UtilLex.chaineIdent(UtilLex.numIdCourant));
			nbrPara = desc.getRefNbParam(indice);
			desc.modifRefNbParam(indice, nbrPara+1);
			placeIdent(-1, PARAMMOD, tCour, cptParam++);
			tabSymb[bc+1].info=cptParam;
			break;
		case 252:
			int k = desc.getNbDef();
			int l = it;
			while(l>0 && k>0) {
				if(tabSymb[l].categorie==PROC && desc.getDefNomProc(k).equals(UtilLex.chaineIdent(tabSymb[l].code))) {
					desc.modifDefNbParam(k, tabSymb[l+1].info);
					desc.modifDefAdPo(k, tabSymb[l].info);
					l=it;
					k--;
				}
				l--;
			}
			if(l<1) {
				UtilLex.messErr("procedure dans Def non définie");
				UtilLex.arret();
			}
			
				
			break;
		case 253:
			desc.setTailleCode(po.getIpo());
			po.constObj();
			po.constGen();
			desc.ecrireDesc(UtilLex.nomSource);
			afftabSymb();
			break;
		case 254:
			if(bc!=1) {
				po.produire(RETOUR);
				po.produire(cptParam);
				while(tabSymb[it].categorie == VARLOCALE || tabSymb[it].categorie == CONSTANTE)
					it--;
				for(int j = it; j >= bc; j--) {
					tabSymb[j].code = -1;
				}
			}
				bc = 1;
				cptParam = 0;  
				break;
		case 255 :
				po.produire(ARRET);
				desc.setTailleCode(po.getIpo());
				po.constObj();
				po.constGen();
				desc.ecrireDesc(UtilLex.nomSource);
				afftabSymb();
			// affichage de la table des symboles en fin de compilation
			break;

		
		default:
			System.out.println("Point de generation non prevu dans votre liste");
			break;

		}
	}
}
    
    
    
    
    
    
    
    
    
    
    
    
    
 
