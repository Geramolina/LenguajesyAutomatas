package Proyecto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class Intermedio {
	ArrayList<Identificador> aux;//Aqui voy a tener las variables que se declararon y sus valores
	ArrayList<String> temporales;//En esta colocare las variables temporales de las operaciones de ensamblador intercambiando la original
	ArrayList<String> pila;//Aqui van estar las operaciones asi como tambien los numeros del arraylist en caso de que venga parentesis
	boolean ban=false;//Me va ayudar a parar el flujo cuando sea las declaraciones de variables en ensamblador y las operaciones normales
	int ct=0;//Este va ser mi contador de variables temporales
	static String asm="", ensambla="";//Uno de ellos es donde aguardare el codigo ensamblador y en la otra el nombre de la variable temporal donde se guardara el resultado
	StringTokenizer tokenizer;//Me permitira porder dividir las operaciones y efectuarlas por separado
	public Intermedio() {
		temporales=new ArrayList<String>();//Inicializo mis variables temporales 
		aux=Sintactico.TablaSimbolos;//Copeo las tablas de variables al mi array aux para de ahi poder iniciar operaciones
		analizarOperaciones();//Mando a analizar las operaciones de las variables
	}
	public void analizarOperaciones() {
		asm+="TITLE	Programa para evaluar una expresion\n";
		asm+="PAGE	60,132\n";				
		asm+=".586\n";						
		asm+=".MODEL FLAT, STDCALL\n";
		asm+=".STACK\n";	
		asm+=".DATA\n";
		//Checo primeramente los booleanos y los declaro en mi variables ensamblador
		for(int i=0; i<aux.size(); i++) {//Recorro las declaraciones asi como tambien asignaciones
			if(aux.get(i).getTipo().equals("boolean")) {
				int b=0;
				if(aux.get(i).getValor().equals("true"))
					b=1;
				asm+=aux.get(i).getNombre()+" dword "+b+"\n";//Lo guardo en mi variable ensamblador
				aux.remove(i);//Remuevo la posicion por que ya no la necesito la variable por que ya esta inicializada
			}
		}
		for(int i=0; i<aux.size(); i++) {
			if(aux.get(i).getTipo().equals("Clase")) {//Si la variable es de tipo  clase la remuevo ya que no es necesaria
				aux.remove(i);
			}
			//Si es entera la incializo con el valor que se muestra es de tipo int o double
			if(aux.get(i).getTipo().equals("int")||aux.get(i).getTipo().equals("double")) {
				boolean banI=false;
				for(int k=0; k<aux.get(i).getValor().length(); k++) {
					if(Arrays.asList('+','-','/','*').contains(aux.get(i).getValor().charAt(k))) {
						banI=true;
					}
				}
				if(!banI) {
					asm+=aux.get(i).getNombre()+" dword "+aux.get(i).getValor()+"\n";
					aux.remove(i);
					i--;
				}else {
					asm+=aux.get(i).getNombre()+" dword 0\n";
				}
			}
		}
		//En caso de ser String le asigno a mi variable ensamblador la linea de la declaracion en string
		for(int i=0; i<aux.size(); i++) {
			if(aux.get(i).getTipo().equals("String")) {
				asm+=aux.get(i).getNombre()+" byte "+aux.get(i).getValor()+",13,10\n";
				aux.remove(i);
			}
		}
		//Ya pasado los filtros anteriores quiere decir que solo me quedaron asignaciones con operaciones
		for(int i=0; i<aux.size(); i++) {
			//Primeramente incializo mi variable en ensamblador a cero por que despues la necesitaremos para asignarle el resultado
			asm+=aux.get(i).getNombre()+" dword 0\n";
			if(aux.get(i).getTipo().equals("Asignacion")){
				boolean banI=false;
				for(int k=0; k<aux.get(i).getValor().length(); k++) {
					if(Arrays.asList('+','-','/','*').contains(aux.get(i).getValor().charAt(k))) {
						banI=true;
					}
				}
				if(banI) {//Quiere decir que las asignaciones tiene operaciones por lo que paso a hacerlas
					String temp2=Lexico.colocaEspacios(aux.get(i).getValor());
					aux.get(i).setValor(temp2);
					int cont=tokenizer.countTokens();//Saco todos los toquen que voy a tener o fragmentos de operaciones
					for(int k=0; k<cont; k++) {
						String token=tokenizer.nextToken();
						pila.add(token);//Aqui lo voy llenando con las operaciones especificadas en la asignacion del valor
					}
					int sub=pila.indexOf("("), r=0;
					boolean banE=false;
					while(sub!=-1) {
						int subC=pila.indexOf(")");
						ArrayList<String> aux1= new ArrayList<String>();
						for(int j=sub+1;j<subC;j++) {
							String retorno="";
							if(Pattern.matches("^[A-Za-z]+$",pila.get(j)))
								retorno=""+devuelveValor(pila.get(j));
							else
								retorno=pila.get(j);
							aux1.add(retorno);
							temporales.add(retorno);
						}
						if(aux1.get(0).equals("-")) {//-
							aux1.remove(0);//-
							aux1.set(0,"-"+aux1.get(0));//-3
						}
						while(subC>sub) {
							pila.remove(subC);
							subC--;
						}
						r=Integer.parseInt(checa(aux1));
						pila.set(sub,""+r);
						sub=pila.indexOf("(");
					}
					if(banE) {
						String retorno=temporales.get(0);
						temporales.remove(temporales.size()-1);
						temporales.add(retorno);
					}else {
						temporales=(ArrayList<String>) pila.clone();
						r=Integer.parseInt(checa(pila));
						Sintactico.TablaSimbolos.get(i).setValor(""+r);
						asm+="\n;Asignacion\n";
						asm+="mov eax , "+ensambla+"\n";
						asm+="mov "+aux.get(i).getNombre()+" , eax\n";
						temporales.removeAll(temporales);
					}
					if(pila.size()>1) {
						String retorno=temporales.get(0);
						temporales=(ArrayList<String>) pila.clone();
						temporales.remove(temporales.size()-1);
						temporales.add(retorno);
						r=Integer.parseInt(checa(pila));
						Sintactico.TablaSimbolos.get(i).setValor(""+r);
						asm+="\n;Asignacion\n";
						asm+="mov eax , "+ensambla+"\n";
						asm+="mov "+aux.get(i).getNombre()+" , eax\n";
						temporales.removeAll(temporales);
					}
				}else {		
					asm+="\n;Asignacion\n";
					asm+="mov eax , "+aux.get(i).getValor()+"\n";
					asm+="mov "+aux.get(i).getNombre()+" , eax\n";
				}
			}
			if(!aux.get(i).getTipo().equals("String") || !aux.get(i).getTipo().equals("boolean")) {
				String temp=Lexico.colocaEspacios(aux.get(i).getValor());
				aux.get(i).setValor(temp);
				//Ahora comienzo a ver la jerarquia de operaciones tomando en cuenta parentesis
				//Nuestro lenguaje no acepta parentesis anidados
				tokenizer=new StringTokenizer(temp);
				int cont=tokenizer.countTokens();//Saco todos los toquen que voy a tener o fragmentos de operaciones
				pila= new ArrayList<String>();//Lo guardo para realizar la jerarquia de operaciones
				for(int k=0; k<cont; k++) {
					//String token=tokenizer.nextToken();
					pila.add(tokenizer.nextToken());
					//pila.add(token);//Aqui lo voy llenando con las operaciones especificadas en la asignacion del valor
				}
				Novariables();
				//Primera jerarquia son parentesis
				int sub=pila.indexOf("(");
				String r="";
				boolean banE=false;
				while(sub!=-1) {
					banE=true;
					int subC=pila.indexOf(")");
					ArrayList<String> aux1= new ArrayList<String>();
					for(int j=sub+1;j<subC;j++) {
						String retorno="";
						if(Pattern.matches("^[A-Za-z]+$",pila.get(j)))
							retorno=""+devuelveValor(pila.get(j));
						else
							retorno=pila.get(j);
						aux1.add(retorno);
						temporales.add(retorno);
					}
					if(aux1.get(0).equals("-")) {//-
						aux1.remove(0);//-
						aux1.set(0,"-"+aux1.get(0));//-3
					}
					while(subC>sub) {
						pila.remove(subC);
						subC--;
					}
					r=checa(aux1);
					pila.set(sub,""+r);
					sub=pila.indexOf("(");
				}
				if(banE) {
					String retorno=temporales.get(0);
					temporales.remove(temporales.size()-1);
					temporales.add(retorno);
				}else {
					temporales=(ArrayList<String>) pila.clone();
					r=checa(pila);
					Sintactico.TablaSimbolos.get(i).setValor(""+r);
					asm+="\n;Asignacion\n";
					asm+="mov eax , "+ensambla+"\n";
					asm+="mov "+aux.get(i).getNombre()+" , eax\n";
					temporales.removeAll(temporales);
				}
				if(pila.size()>1) {
					String retorno=temporales.get(0);
					temporales=(ArrayList<String>) pila.clone();
					temporales.remove(temporales.size()-1);
					temporales.add(retorno);
					r=checa(pila);
					Sintactico.TablaSimbolos.get(i).setValor(""+r);
					asm+="\n;Asignacion\n";
					asm+="mov eax , "+ensambla+"\n";
					asm+="mov "+aux.get(i).getNombre()+" , eax\n";
					temporales.removeAll(temporales);
				}
			}
		}
		asm+="; Regresar al SO\n";
		asm+="INVOKE ExitProcess , 0\n";
		asm+="Principal		ENDP\n";	
		asm+="END		Principal\n";
	}
	public int devuelveValor(String variable) {
		int res=0;
		for(int i=0; i<Sintactico.TablaSimbolos.size(); i++) {
			if(Sintactico.TablaSimbolos.get(i).getNombre().equals(variable))
				res=Integer.parseInt(Sintactico.TablaSimbolos.get(i).getValor());
		}
		return res;
	}
	public String checa(ArrayList<String> valores) {
		for(int i=0; i<valores.size(); i++) {
			if(valores.get(i).equals("/")||valores.get(i).equals("*")) {
				int temp=operacion(valores.get(i-1),valores.get(i+1),valores.get(i).charAt(0), i);
				if(temp<0) {
					if(i-2<0) {
						//System.out.println(valores.get(i));
						valores.set(i, ""+temp);
						valores.remove(i+1);
						temporales.set(i, ensambla);
						temporales.remove(i+1);
						i--;
						//i-1 Cuando se guarda el ultimo resultado
					}else {//System.out.println(valores.get(i));
						//i-2 Cuando estamos en proceso de obtener el ultimo resultado
						//valores.set(i-2,"-");
						//temp=temp*-1;
						valores.set(i-1,""+temp);
						valores.remove(i);
						valores.remove(i);
						temporales.set(i-1, ensambla);
						temporales.remove(i);
						temporales.remove(i);
						i--;
					}
				}else {
					valores.set(i-1,""+temp);
					valores.remove(i+1);
					valores.remove(i);
					temporales.set(i-1,""+ensambla);
					temporales.remove(i+1);
					temporales.remove(i);
					i--;
				}
			}
		}
		for(int i=0; i<valores.size(); i++) {
			if(valores.get(i).equals("+")||valores.get(i).equals("-")) {
				int temp=operacion(valores.get(i-1),valores.get(i+1),valores.get(i).charAt(0),i);
				if(temp<0) {
					if(i-2<0) {
						valores.set(i, ""+temp);//
						valores.remove(i+1);
						temporales.set(i, ensambla);
						temporales.remove(i+1);
					}else {
						valores.set(i-1,""+temp);
						valores.remove(i);
						valores.remove(i);
						temporales.set(i-1,""+ensambla);
						temporales.remove(i);
						temporales.remove(i);
					}
				}else {
					valores.set(i-1,""+temp);
					valores.remove(i+1);
					valores.remove(i);
					temporales.set(i-1,""+ensambla);
					temporales.remove(i+1);
					temporales.remove(i);
					i--;//Para que vuelva a repetirse el bucle ya que se actualizo la posicion
				}
			}
		}
		return valores.get(0);
	}
	public int operacion(String np,String ns,char o, int pos)
	{
		String v2="",v3="";
		int	resultado=0, n1=Integer.parseInt(np),n2=Integer.parseInt(ns);
		switch(o)
		{
		case '*':
			if(!ban) {
				for(int i=0; i<ct+1;i++)
					asm+="temp"+i+" dword 0\n";
				ct=0;
				asm+="\n .CODE\n";
				asm+="Principal     Proc\n\n";
				asm+="; Operacion de multiplicacion\n";
				asm+="mov eax , "+temporales.get(pos+1)+"\n";
				asm+="mov temp"+ct+" , eax\n";
				asm+="mov eax , "+temporales.get(pos-1)+"\n";
				asm+="imult temp"+ct+"\n";
				ct++;
				asm+="mov temp"+ct+" , eax\n";
				ensambla="temp"+ct;
				ban=true;
			}else{
				asm+="\n; Operacion de multiplicacion\n";
				ct++;
				//asm+="dword temp"+ct+" 0\n";
				asm+="mov eax , "+temporales.get(pos-1)+"\n";
				asm+="mov temp"+ct+" , eax\n";
				ct++;
				//asm+="dword temp"+ct+" 0\n";
				asm+="mov eax , "+temporales.get(pos+1)+"\n";
				asm+="mov temp"+ct+" , eax\n";
				asm+="mov eax , temp"+(ct-1)+"\n";
				asm+="imult temp"+ct+"\n";
				ct++;
				ensambla="temp"+ct;
				//asm+="dword "+ensambla+" 0\n";
				asm+="mov "+ensambla+" , eax\n";
			}
			resultado=n1*n2;
			break;
		case '/':
			if(!ban) {
				for(int i=0; i<ct+1;i++)
					asm+="temp"+i+" dword 0\n";
				ct=0;
				asm+="\n .CODE\n";
				asm+="Principal     Proc\n\n";
				asm+="; Operacion de division\n";
				asm+="mov eax , "+temporales.get(pos+1)+"\n";
				asm+="mov temp"+ct+" , eax\n";
				asm+="mov eax , "+temporales.get(pos-1)+"\n";
				asm+="div temp"+ct+"\n";
				ct++;
				asm+="mov temp"+ct+" , eax\n";
				ensambla="temp"+ct;
				ban=true;
			}else{
				asm+="\n; Operacion de division\n";
				ct++;
				//asm+="dword temp"+ct+" 0\n";
				asm+="mov eax , "+temporales.get(pos-1)+"\n";
				asm+="mov temp"+ct+" , eax\n";
				ct++;
				//asm+="dword temp"+ct+" 0\n";
				asm+="mov eax , "+temporales.get(pos+1)+"\n";
				asm+="mov temp"+ct+" , eax\n";
				asm+="mov eax , temp"+(ct-1)+"\n";
				asm+="div temp"+ct+"\n";
				ct++;
				ensambla="temp"+ct;
				//asm+="dword "+ensambla+" 0\n";
				asm+="mov "+ensambla+" , eax\n";
			}
			resultado=n1/n2;
			break;
		case '+': 
			if(!ban) {
				for(int i=0; i<ct+1;i++)
					asm+="temp"+i+" dword 0\n";
				ct=0;
				asm+="\n .CODE\n";
				asm+="Principal     Proc\n\n";
				asm+="; Operacion de suma\n";
				asm+="mov eax , "+temporales.get(pos+1)+"\n";
				asm+="mov temp"+ct+" , eax\n";
				asm+="mov eax , "+temporales.get(pos-1)+"\n";
				asm+="add temp"+ct+"\n";
				ct++;
				asm+="mov temp"+ct+" , eax\n";
				ensambla="temp"+ct;
				ban=true;
			}else {
				asm+="\n; Operacion de Suma\n";
				ct++;
				//asm+="dword temp"+ct+" 0\n";
				asm+="mov eax , "+temporales.get(pos-1)+"\n";
				asm+="mov temp"+ct+" , eax\n";
				ct++;
				//asm+="dword temp"+ct+" 0\n";
				asm+="mov eax , "+temporales.get(pos+1)+"\n";
				asm+="mov temp"+ct+" , eax\n";
				asm+="mov eax , temp"+(ct-1)+"\n";
				asm+="add temp"+ct+"\n";
				ct++;
				ensambla="temp"+ct;
				//asm+="dword "+ensambla+" 0\n";
				asm+="mov "+ensambla+" , eax\n";
			}
			resultado=n1+n2;
			break;
		case '-':
			if(!ban) {
				for(int i=0; i<ct+1;i++)
					asm+="temp"+i+" dword 0\n";
				ct=0;
				asm+="\n .CODE\n";
				asm+="Principal     Proc\n\n";
				asm+="; Operacion de resta\n";
				asm+="mov eax , "+temporales.get(pos+1)+"\n";
				asm+="mov temp"+ct+" , eax\n";
				asm+="mov eax , "+temporales.get(pos-1)+"\n";
				asm+="sub temp"+ct+"\n";
				ct++;
				asm+="mov temp"+ct+" , eax\n";
				ensambla="temp"+ct;
				ban=true;
			}else {
				asm+="\n; Operacion de resta+\n";
				ct++;
				//asm+="dword temp"+ct+" 0\n";
				asm+="mov eax , "+temporales.get(pos-1)+"\n";
				asm+="mov temp"+ct+" , eax\n";
				ct++;
				//asm+="dword temp"+ct+" 0\n";
				asm+="mov eax,"+temporales.get(pos+1)+"\n";
				asm+="mov temp"+ct+" , eax\n";
				asm+="mov eax , temp"+(ct-1)+"\n";
				asm+="sub temp"+ct+"\n";
				ct++;
				ensambla="temp"+ct;
				//asm+="dword "+ensambla+" 0\n";
				asm+="mov "+ensambla+" , eax\n";
			}
			resultado=n1-n2;
			break;
		}
		return resultado;
	}
	public void Novariables() {
		for(int i=0;i<Lexico.tokenAnalizados.size(); i++) {
			if(Arrays.asList("+","/","*","-").contains(Lexico.tokenAnalizados.get(i).getValor())) {
				ct++;
			}
		}
		ct*=3;
	}
	public void CambiaValores(ArrayList<String> copia) {
		for(int i=0; i<copia.size(); i++) {
			if(Pattern.matches("^[A-Za-z]+$",copia.get(i))) {
				int pos=0;
				for(int k=0; k<Semantico.declaraciones.size();k++) {
					if(Semantico.declaraciones.get(k).getNombre().equals(copia.get(i)))
						pos=k;
				}
				copia.set(i,Semantico.declaraciones.get(pos).getValor());
			}
		}
	}
}
