import java.io.*;
 //TODO : Renseigner le champs auteur : Nom1_Prenom1_Nom2_Prenom2_Nom3_Prenom3
 /**
 * 
 * @author Hermenier Pierre-Edouard, Nazih Asmaa, Ben Lamlih Zidane 
 * @version 2020
 *
 */


public class Edl {
	
	static class Defligne{
		String nomProc;
		int adPo, nbParam;
		public Defligne(String nomProc, int adPo, int nbParam) {
			this.nomProc = nomProc;
			this.adPo = adPo;
			this.nbParam = nbParam;
		}
	}
	
	static class VecteurT{
		int po, translation;
		public VecteurT(int po, int translation) {
			this.po = po;
			this.translation = translation;
		}
	}
	// nombre max de modules, taille max d'un code objet d'une unite
	static final int MAXMOD = 5, MAXOBJ = 1000;
	// nombres max de references externes (REF) et de points d'entree (DEF)
	// pour une unite
	private static final int MAXREF = 10, MAXDEF = 10;

	// typologie des erreurs
	private static final int FATALE = 0, NONFATALE = 1;

	// valeurs possibles du vecteur de translation
	private static final int TRANSDON=1,TRANSCODE=2,REFEXT=3;

	// table de tous les descripteurs concernes par l'edl
	static Descripteur[] tabDesc = new Descripteur[MAXMOD + 1];

	//TODO : declarations de variables A COMPLETER SI BESOIN
	static int ipo, nMod, nbErr;
	static String nomProg;
	static int[][] tabDec;
	static Defligne[] dicoDef;
	static int[][] adFinale;
	static int ind;
	static String[] tabName;
	static int indName;
	static VecteurT[] vecteurs; 

	// utilitaire de traitement des erreurs
	// ------------------------------------
	static void erreur(int te, String m) {
		System.out.println(m);
		if (te == FATALE) {
			System.out.println("ABANDON DE L'EDITION DE LIENS");
			System.exit(1);
		}
		nbErr = nbErr + 1;
	}

	// utilitaire de remplissage de la table des descripteurs tabDesc
	// --------------------------------------------------------------
	static void lireDescripteurs() {
		String s;
		System.out.println("les noms doivent etre fournis sans suffixe");
		System.out.print("nom du programme : ");
		s = Lecture.lireString();
		tabName[indName++] = s;
		tabDesc[0] = new Descripteur();
		tabDesc[0].lireDesc(s);
		if (!tabDesc[0].getUnite().equals("programme"))
			erreur(FATALE, "programme attendu");
		nomProg = s;

		nMod = 0;
		while (!s.equals("") && nMod < MAXMOD) {
			System.out.print("nom de module " + (nMod + 1)
					+ " (RC si termine) ");
			s = Lecture.lireString();
			if (!s.equals("")) {
				tabName[indName++] = s;
				nMod = nMod + 1;
				tabDesc[nMod] = new Descripteur();
				tabDesc[nMod].lireDesc(s);

				if (!tabDesc[nMod].getUnite().equals("module"))
					erreur(FATALE, "module attendu");
			}
		}
	}


	static void constMap() {
		// f2 = fichier executable .map construit
		OutputStream f2 = Ecriture.ouvrir(nomProg + ".map");
		if (f2 == null)
			erreur(FATALE, "creation du fichier " + nomProg
					+ ".map impossible");
		// pour construire le code concatene de toutes les unités
		int[] po = new int[(nMod + 1) * MAXOBJ + 1];
		
		for(int i=0; i< nMod+1; i++) {
			InputStream fObj = Lecture.ouvrir(tabName[i]+".obj");
			vecteurs = new VecteurT[tabDesc[i].getNbTransExt()];
			for(int j=0; j< tabDesc[i].getNbTransExt(); j++) {
				String tab[] = new String[2];
				tab = Lecture.lireString(fObj).split("   ");
				vecteurs[j] = new VecteurT(Integer.parseInt(tab[0]),Integer.parseInt(tab[1]));
				System.out.println("indice : " + Integer.parseInt(tab[0]) + " codeVect: " + Integer.parseInt(tab[1]));
			}

			int indiceVect = 0;
			
			for(int j = 0; j < tabDesc[i].getTailleCode(); j++) {
				if(ipo==2) {
					po[ipo] = tabDec[0][nMod] + tabDesc[nMod].getTailleGlobaux();
					Lecture.lireIntln(fObj);
				}
				else if(indiceVect < vecteurs.length && vecteurs[indiceVect].po==j+1) {
					//application des vecteurs de transalations
					switch(vecteurs[indiceVect].translation) {
						case TRANSDON : po[ipo] = tabDec[0][i] + Lecture.lireIntln(fObj);
										indiceVect++; 
										break;
						case TRANSCODE :po[ipo] = tabDec[1][i] + Lecture.lireIntln(fObj);
										indiceVect++;
										break;
						case REFEXT : 
										po[ipo] = adFinale[i][Lecture.lireIntln(fObj)];
										indiceVect++;
										break;
						default : System.out.println("Error typage vecteur translation");
					}
				}
				else
					po[ipo]=Lecture.lireIntln(fObj);
				ipo++;
			}
			Lecture.fermer(fObj);
		}
		
		for (int i = 1; i <= ipo; i++)
			Ecriture.ecrireStringln(f2, "" + po[i]);
		Ecriture.fermer(f2);

		// creation du fichier en mnemonique correspondant
		Mnemo.creerFichier(--ipo, po, nomProg + ".ima");
	}
	

	public static void main(String argv[]) {
		System.out.println("EDITEUR DE LIENS / PROJET LICENCE");
		System.out.println("---------------------------------");
		System.out.println("");
		nbErr = 0;
		tabName = new String[6];
		indName = 0;
		ipo=1;

		// Phase 1 de l'edition de liens
		// -----------------------------
		lireDescripteurs();	
		tabDec = new int[2][nMod+1];
		dicoDef = new Defligne[60];
		adFinale = new int[nMod+1][];
		ind = 1;
		for(int i = 0; i < nMod + 1 ; i++) {
			if(i==0) {
				tabDec[0][0] = 0;
				tabDec[1][0] = 0;
			}else {
				tabDec[0][i] = tabDesc[i-1].getTailleGlobaux() + tabDec[0][i-1]; 
			    tabDec[1][i] = tabDesc[i-1].getTailleCode() + tabDec[1][i-1];
			}
			
			for(int j = 1; j<= tabDesc[i].getNbDef(); j++) {
				int k = 1;
				boolean insert = false;
				while(k<ind && !insert) {
					if(dicoDef[k].nomProc.equals(tabDesc[i].getDefNomProc(j)))
						insert = true;
					else
						k++;
				}
				if(k==ind && !insert) {
					dicoDef[ind] = new Defligne(tabDesc[i].getDefNomProc(j), tabDesc[i].getDefAdPo(j) + tabDec[1][i], tabDesc[i].getDefNbParam(j));
					ind++;
				}
			}
		}
		
		for(int i = 0; i < adFinale.length; i++ ) {
			adFinale[i] = new int[tabDesc[i].getNbRef()+1];
			for(int j=1; j<=tabDesc[i].getNbRef(); j++) {
				int k = 1;
				while(k < ind) {
					if(dicoDef[k].nomProc.equals(tabDesc[i].getRefNomProc(j)))
						adFinale[i][j] = dicoDef[k].adPo;
					k++;
				}
			}
		}
		
		verifRef();
		if (nbErr > 0) {
			System.out.println("programme executable non produit");
			System.exit(1);
		}
		
		for(int i = 0; i < adFinale.length; i++ ) {
			for(int j = 0; j< adFinale[i].length; j++) {
				System.out.println(adFinale[i][j]);
			}
			System.out.println();
		}
		
		// Phase 2 de l'edition de liens
		// -----------------------------
		constMap();				//TODO : ... A COMPLETER ...
		System.out.println("Edition de liens terminee");
	}
	
	static void verifRef() {
		for(int i = 0; i < nMod+1; i++) {
			for(int j = 1; j<= tabDesc[i].getNbRef(); j++) {
				boolean insert = false;
				int ind = 1;
				while(dicoDef[ind]!=null && !insert) {
					if(dicoDef[ind].nomProc.equals(tabDesc[i].getRefNomProc(j))) {
						insert = true;
						if(dicoDef[ind].nbParam != tabDesc[i].getRefNbParam(j)) { 
							erreur(NONFATALE, "Ref : " + tabDesc[i].getRefNomProc(j) + " mauvais nombre de paramètre(s)");
						}
					}
					ind++;
				}
				if(!insert)
					erreur(NONFATALE, "Ref : " + tabDesc[i].getRefNomProc(j) + " non definie");
			}
		}
	}
}
