package Proyecto;
import java.io.*;
import java.time.temporal.Temporal;
import java.util.*;

import org.omg.Messaging.SyncScopeHelper;
public class Objeto {
	ArrayList<String> prefijos=new ArrayList<String>();
	ArrayList<String> temporal;
	String mov="",add="", resta="", div="", mult="";//Sentencias de operacion
	static String comienzo="";
	public Objeto() {
		inicializar();//Inicializo variables que necesitaremos
		crearArchivoObjeto();
	}
	public void inicializar() {
		//Donde guardare cada una de las sentencias de ensamblador
		prefijos=new ArrayList<String>();
		//Inicializo los valores estaticos de las operaciones
		mov="11000110";
		add="10000011";
		resta="10010100";
		div="11110110";
		mult="10001001";
		//Coloco el programa de inicio en mi archivo objeto
		comienzo+="Primera sección\n";
		comienzo+="--------------------------------------------------------------------------------------------------------------\n";
		comienzo+="10111010 11110100 00011111\n";//Titulo
		comienzo+="10010010 00111100 10000100\n"; //Posteriormente las paginas y enseguida el numero de pagina
		comienzo+="00101110 00000010 01001010\n";//Traspasamos la informacion de lo que es el tipo de procesador que .586
		comienzo+="00101110 01001101 01001111 01000100 01000101 01001100 01000110 \n";//.MODEL	FLAT, STDCALL
		comienzo+="01001100 01000001 01010100 00101100 00100000 01010011 01010100 01000100 01000011 01000001 01001100 01001100";
		comienzo+="00101110 01110011 01110100\n";//.stack
		comienzo+="00101110\n";//.data
		comienzo+="Segunda seccion(Declaraciones de variables)\n";
		comienzo+="--------------------------------------------------------------------------------------------------------------\n";
	}
	public void crearArchivoObjeto() {
		//Primeramente en cronologia con nuestra estructura de programa colocamos las declaraciones
		StringTokenizer tokenizer = new StringTokenizer(Intermedio.asm);
		int cont=tokenizer.countTokens();
		temporal= new ArrayList<String>();
		for(int i=0; i<cont; i++) {
			String aux=tokenizer.nextToken();
			temporal.add(aux);//Guardo cada estructura y sentencia en un arraylist para posteriormente analizarla		
		}
		//Me posiciono donde se encuentra el .data que es donde a partir de ahi estan las declaraciones
		//Y donde terminan, es decir en el Proc
		int sub=temporal.indexOf(".DATA"), fin=temporal.indexOf(".CODE");
		do {
			if(temporal.get(sub).equals("dword")) {
				String aux=Integer.toBinaryString(Integer.parseInt(temporal.get(sub+1))), temp="";
				for(int i=0; i<(8-aux.length()); i++)
					temp+="0";
				temp+=aux;
				comienzo+="00001010 "+ temp+" \n";//Segun archivo de visual studio el dword lo lee asigna un 10 en hexadecimal A + constante
			}
			sub++;
		}while(sub!=fin);
		//Posteriormente sigue el encabezado principal y la etiqueta de codificacion
		comienzo+="00010100\n";
		comienzo+=temporal.get(sub+1)+" "+temporal.get(sub+2)+"\n\n";
		comienzo+="Tercera seccion(Procedimiento)\n";
		comienzo+="--------------------------------------------------------------------------------------------------------------\n";
		for(int i=0; i<obtenerNumero(); i++) {
			sub=encuentrafin(sub);//Saco donde se encuentra el primer ;
			fin=encuentrafin(sub+1);//Saco donde se encuentra el ultimo;
			do {
				if(fin==-1)//Quiere decir que no lo encontre
					break;
				if(temporal.get(sub).equals("mov")) {//Puede venir un mov
					String temp="";
					comienzo+=""+mov+" ";//Asigno los datos binarios de mov
					if(temporal.get(sub+1).equals("eax")) {//Seguido de un registro
						comienzo+="11000000 ";
						if(temporal.get(sub+3).length()>2) {
							if(temporal.get(sub+3).substring(0,3).equals("tem")) { //Seguido de una variable
								temp=temporal.get(sub+3).substring(temporal.get(sub+3).length()-2,temporal.get(sub+3).length());//Extraigo el nombre de variable
								temp=checaNumero(temp);//Mando analizar el numero
								comienzo+=temp+" ";//Concateno el numero
								comienzo+="\n";
								sub++;
								continue;
							}
						}else {//Quiere decir que es una constante
							comienzo+=checaNumero(temporal.get(sub+3))+" ";
							sub++;
							comienzo+="\n";
							continue;
						}
					}
					if(temporal.get(sub+1).substring(0,1).equals("t")) {//Seguido de una variable
						temp=temporal.get(sub+1).substring(temporal.get(sub+1).length()-2,temporal.get(sub+1).length());//Extraigo el nombre de variable
						temp=checaNumero(temp);//Mando analizar el numero
						comienzo+=temp+" ";//Concateno el numero
						if(temporal.get(sub+3).substring(0,3).equals("eax")) {//Seguir un registro
							comienzo+="11000001  ";//Concateno el numero binario referente al registro con w
							comienzo+="\n";
						}
						sub++;
						continue;
					}
					if(temporal.get(sub+1).length()==1) {//Seguido de una variable
						comienzo+=Integer.toBinaryString(98)+" ";//Concateno el numero
						if(temporal.get(sub+3).substring(0,3).equals("eax")) {//Seguir un registro
							comienzo+="11000001  ";//Concateno el numero binario referente al registro con w
						}
					}
				}
				if(temporal.get(sub).equals("add")) {
					String temp="";
					comienzo+=add+" ";
					temp=temporal.get(sub+1).substring(temporal.get(sub+1).length()-2,temporal.get(sub+1).length());//Extraigo el nombre de variable
					temp=checaNumero(temp);//Mando analizar el numero
					comienzo+=temp+" ";//Concateno el numero
				}
				if(temporal.get(sub).equals("imult")) {
					String temp="";
					comienzo+=mult+" ";
					temp=temporal.get(sub+1).substring(temporal.get(sub+1).length()-2,temporal.get(sub+1).length());//Extraigo el nombre de variable
					temp=checaNumero(temp);//Mando analizar el numero
					comienzo+=temp+" ";//Concateno el numero
				}
				if(temporal.get(sub).equals("sub")) {
					String temp="";
					comienzo+=resta+" ";
					temp=temporal.get(sub+1).substring(temporal.get(sub+1).length()-2,temporal.get(sub+1).length());//Extraigo el nombre de variable
					temp=checaNumero(temp);//Mando analizar el numero
					comienzo+=temp+" ";//Concateno el numero
				}
				if(temporal.get(sub).equals("div")) {
					String temp="";
					comienzo+=div+" ";
					temp=temporal.get(sub+1).substring(temporal.get(sub+1).length()-2,temporal.get(sub+1).length());//Extraigo el nombre de variable
					temp=checaNumero(temp);//Mando analizar el numero
					comienzo+=temp+" ";//Concateno el numero
				}
				sub++;
				comienzo+="\n";
			}while(sub!=fin);
		}
		System.out.println(comienzo);
		//Terminar realizacion de proceso de cambio a binario
		comienzo+="Cuarta seccion(final)\n";
		comienzo+="--------------------------------------------------------------------------------------------------------------\n";
	    comienzo+="01010000 00000000\n";
	    comienzo+="00010000 10010100 00001000\n";
	    comienzo+="00000000 00000000";
	    
	}
	public String checaNumero(String num) {
		String num2="", regreso="";
		for(int i=0; i<num.length(); i++)
			if(num.charAt(i)=='1'||num.charAt(i)=='2'||num.charAt(i)=='3'||num.charAt(i)=='4'||num.charAt(i)=='5'||num.charAt(i)=='6'||num.charAt(i)=='7'||num.charAt(i)=='8'||num.charAt(i)=='9'||num.charAt(i)=='0') {
				num2+=""+num.charAt(i);
		}
		num2=Integer.toBinaryString(Integer.parseInt(num2));
		for(int i=0; i<(8-num2.length()); i++)
			regreso+="0";
		regreso+=num2;
		return regreso;
	}
	public int obtenerNumero() {
		int c=0;
		for(int i=0;i<temporal.size(); i++) {
			if(temporal.get(i).equals(";"))
				c++;
		}
		return c;
	}
	public int encuentrafin(int sub) {
		for(int i=sub; i<temporal.size(); i++) {
			if(temporal.get(i).equals(";"))
				return i;
		}
		return -1;
	}
}
