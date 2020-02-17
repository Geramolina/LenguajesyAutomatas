package Proyecto;

public class Token 
{
	private String tipo;
	private String valor;
	private int NumTipo;
	public Token(int Num,String t, String v) {
		tipo=t;
		valor=v;
		NumTipo=Num;
	}
	public Token(String t, String v) {
		tipo=t;
		valor=v;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public String getValor() {
		return valor;
	}
	public int getNumTipo() {
		return NumTipo;
	}
	public void setNumTipo(int numTipo) {
		NumTipo = numTipo;
	}
	public void setValor(String valor) {
		this.valor = valor;
	}
	public String toString() {
		return getTipo()+" --> "+getValor();
	}
}
