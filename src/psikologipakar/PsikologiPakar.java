package psikologipakar;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Scanner;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class PsikologiPakar {
    public static int usia, wmKe=1, ruleKe=1, batasProb=12, jumlahRule=0;
    public static Connection conn = null;
    public static String jawaban, dbName="sistempakarpsikologianak";
    public static Statement stmt ;
    public static ResultSet ruleFire, ruleFire2, wm, rsPertanyaan ;
    public static BufferedReader huruf=new BufferedReader(new InputStreamReader (System.in));
    public static Scanner angka=new Scanner (System.in);
    public static void init(){
	try{
	   String sql;
	   String [] table={"fire","wm"};
	   stmt = conn.createStatement();
	   for (int i=0; i<2; i++){
		sql = "DELETE FROM "+table[i];
		stmt.executeUpdate(sql);
	   }
	   sql="delete from "+dbName+"."+"probabilitas"+" where "+"probabilitas"+"."+"nomor"+">"+batasProb+"";
	   stmt.executeUpdate(sql);
	   sql="delete from hitungcf";
	   stmt.executeUpdate(sql);
	}
	catch (SQLException ex){
            System.err.println("SQLException:"+ex.getMessage());
       }
    }
    public static void connectDB() throws ClassNotFoundException, IOException{
	    String driver = "com.mysql.jdbc.Driver";
	    String url = "jdbc:mysql://localhost:3306/"+dbName; // sesuaikan dengan nama database anda
            String user = "root"; //user mysql 
            String pass = ""; //password mysql
	    try {
		Class.forName(driver);
		conn = DriverManager.getConnection(url,user,pass);
	    } 
	    catch (SQLException e) {
		System.out.println("SQLException: "+e.getMessage());
		//System.out.println("SQLState: "+e.getSQLState());
		//System.out.println("VendorError: "+e.getErrorCode());
	    }
    }
    public static void addWM(String wm){
	try{
	   stmt = conn.createStatement();
           String sql = "insert into wm values('"+wmKe+"','"+wm+"')";
           stmt.executeUpdate(sql);
	   wmKe++;
	}
	catch (SQLException ex){
            System.err.println("SQLException:"+ex.getMessage());
       }
    }
    public static void fireRule(String fire){
	try{
	   stmt = conn.createStatement();String sql;
	   /*if (fire.equals("1")||fire.equals("2"))
		sql = "insert into fire values('"+ruleKe+"','"+fire+"',"+1+")";
	   else*/
	       sql = "insert into fire values('"+ruleKe+"','"+fire+"',"+0+")";
           stmt.executeUpdate(sql);
	   ruleKe++;
	}
	catch (SQLException ex){
            System.err.println("SQLException:"+ex.getMessage());
       }
    }
    public static void pertanyaan(String pasien) throws IOException, SQLException{
	stmt = conn.createStatement();Statement stmt2 = conn.createStatement();ResultSet rs2;
	   boolean ulang=true;
           String tambah;String tambah2; String sql;
	   //while (ulang==true){
		sql = "select * from pertanyaan where id='"+pasien+"'";
		rsPertanyaan = stmt.executeQuery(sql);
		while (rsPertanyaan.next()){
                    //UI.pertanyaan.pertanyaan.setText(rsPertanyaan.getString("pertanyaan"));
		    System.out.println(rsPertanyaan.getString("pertanyaan")+" (y/t)");
                        jawaban=huruf.readLine();
			if (jawaban.equals("y")){
                            boolean tidakKosong=true;
			    tambah=rsPertanyaan.getString("tambah");
			    BufferedReader add=new BufferedReader(new InputStreamReader(new ByteArrayInputStream(tambah.getBytes())));
			    tambah2=add.readLine();
			    while (tidakKosong==true){
				sql="select wm from wm";
				rs2 = stmt2.executeQuery(sql);
				boolean x=true;
                                //cek apakah kesimpulan sudah ada di wm//
				while(rs2.next()){
                                    String temp = rs2.getString("wm");
				    if (tambah2.equals(temp))
					x=false;
				}
				if (x==true)
				    addWM(tambah2);
				tambah2=add.readLine();//disini ngebugnya
                                if (tambah2.equals(" ")){
                                    tidakKosong=false;
                                    System.out.println("x");
                                }
			    }
                            
		    }
		}
    }
    public static void cekRule(String cek) throws SQLException{
	Statement stmt1 = conn.createStatement(),stmt3=conn.createStatement(),stmt2 = conn.createStatement();
	String temp, temp2;
	ResultSet rs,rs2;
	rs=stmt2.executeQuery("select * from rule where if1='"+cek+"' or if2='"+cek+"' or if3='"+cek+"' or if4='"+cek+"'");
	while (rs.next()){
	    boolean[] add={false, false, false, false};
	    for (int i=1; i<5; i++){
		String a=Integer.toString(i);
		String b="if"+a;
		temp2=rs.getString(b);
		if (temp2.equals("1"))
		    add[i-1]=true;
		else{
		    wm=stmt1.executeQuery("select * from wm");
		    while (wm.next()){
			temp=wm.getString("wm");
			if (temp2.equals(temp))
			    add[i-1]=true;
		    }
		}
	    }
	    if ((add[0]&&add[1]==true)&&(add[2]&&add[3]==true)){
		rs2 = stmt3.executeQuery("select wm from wm");
		boolean x=true;
		while(rs2.next()){
		    temp = rs2.getString("wm");
		    if (rs.getString("then").equals(temp))
			x=false;
		    }
		if (x==true){
		    fireRule(rs.getString("nomor"));
		    addWM (rs.getString("then"));
		}
	    }
	}
    }
    public static double cfCombine(double a, double b){
	double cf=0;
	if (a>0&& b>0)
	    cf=a+b*(1-a);
	else if ((a<0&&b>0)||(b<0&&a>0)){
	    double min=0;
	    if (a>b){
		min=b*-1;
		if (a<min)
		    min=a;
	    }
	    else if (b>a){
		min=a*-1;
		if (b<min)
		    min=b;
	    }
	    cf=(a+b)/(1-min);
	}
	else
		cf=a+b*(1+a);
	return cf;
    }
    public static void inferensi() throws SQLException{
	stmt = conn.createStatement();ResultSet rs;
	int i=2;
	while (i<wmKe){
	    rs=stmt.executeQuery("select wm from wm where nomor='"+i+"'");
	    if (rs.next()){
		cekRule(rs.getString("wm"));
		i++;
	    }
	}
    }
    public static void deleteRule(int x) throws SQLException{
        stmt=conn.createStatement();
        stmt.executeUpdate("delete from rule where nomor="+x+"");
        System.out.println("rule telah dihapus");
    }
    public static void updateRule(int x) throws SQLException, IOException{
        String k[]=new String[4]; String kesimpulan; double probabilitas;
        for (int i=0; i<4; i++){
            System.out.println("kondisi "+(i+1)+" : (masukkan 1 jika tidak diperlukan)");
            k[i]=huruf.readLine();
        }
        System.out.print("kesimpulan :");
        kesimpulan=huruf.readLine();
        System.out.print("probabilitas :");
        probabilitas=angka.nextDouble();
        boolean m=cekValid(k);
        if (m==true){
            deleteRule(x);
            Statement stmt1=conn.createStatement();
            stmt1.executeUpdate("insert into rule values ('"+x+"','"+k[0]+"','"+k[1]+"','"+k[2]+"','"+k[3]+"','"+kesimpulan+"','"+probabilitas+"')");
            System.out.println("rule telah diupdate");
        }
        else
            System.out.println("rule tidak bisa diupdate");
    }
    public static boolean cekValid(String k[]) throws SQLException{
        stmt=conn.createStatement();
        boolean valid=true;
        for (int i=0; i<4; i++){
            ResultSet rs1=stmt.executeQuery("select * from fakta");
            boolean ada=false;
            while (rs1.next()){
                if (k[i].equals(rs1.getString("fakta")))
                    ada=true;
            }
            if (ada==false){
                valid=false;
                System.out.println("kondisi ke "+(i+1)+" tidak ada dalam data fakta yang valid");
            }
        }
        return valid;
    }
    public static void addRule() throws SQLException, IOException{
        String k[]=new String[4]; String kesimpulan; double probabilitas;
        for (int i=0; i<4; i++){
            System.out.println("kondisi "+(i+1)+" : (masukkan 1 jika tidak diperlukan)");
            k[i]=huruf.readLine();
        }
        System.out.print("kesimpulan :");
        kesimpulan=huruf.readLine();
        System.out.print("probabilitas :");
        probabilitas=angka.nextDouble();
        boolean x=cekValid(k);
        if (x==true){
            stmt=conn.createStatement();
            stmt.executeUpdate("insert into rule values ('"+(jumlahRule+1)+"','"+k[0]+"','"+k[1]+"','"+k[2]+"','"+k[3]+"','"+kesimpulan+"','"+probabilitas+"')");
            System.out.println("rule telah ditambahkan");
        }
        else
            System.out.println("rule tidak bisa ditambahkan");
    }
    public static void viewRule() throws SQLException {
        stmt=conn.createStatement();int n;
        String temp, then;
        int index; int p=0;
        ResultSet rs1=stmt.executeQuery("select * from rule");
        while (rs1.next()){
            p++;
            index=0;
            String[] k=new String [4];
            n=rs1.getInt("nomor");
            for (int i=1; i<5; i++){
                temp="if"+Integer.toString(i);
                if (!rs1.getString(temp).equals("1")){
                    k[index]=rs1.getString(temp);
                    index++;
                }
            }
            then=rs1.getString("then");
            System.out.print(n+". IF "+k[0]);
            for (int i=1; i<index; i++){
                System.out.print(" AND "+k[i]);
            }
            System.out.print(" THEN "+then+", prob = "+rs1.getDouble("ProbThen"));
            System.out.println();
        }
        jumlahRule=p;
    }
    public static double hitungCF() throws SQLException{
	stmt=conn.createStatement(); ResultSet rs, rs2,rs3,rs4,rs5;Statement stmt2=conn.createStatement(), stmt3=conn.createStatement(), stmt4=conn.createStatement(), stmt5=conn.createStatement();
	double cf=0;
	String temp, cek, sql;
	rs=stmt.executeQuery("select * from fire");
	//rs.next();
	while (rs.next()){
	    int indexCF=1;
	    rs2=stmt2.executeQuery("select * from rule ");
	    while (rs2.next()){
		if (rs.getInt("rule")==rs2.getInt("nomor")){
		    for (int i=1; i<5; i++){
			int k=0;
			int gantiIf=indexCF;
			temp="if"+Integer.toString(i);
			cek=rs2.getString(temp);
			if (cek!="1"){
			    rs3=stmt3.executeQuery("select * from probabilitas");
			    while (rs3.next()){
				if (cek.equals(rs3.getString("pernyataan"))){
				    sql="insert into hitungcf values ('"+indexCF+"','"+rs3.getDouble("probabilitas")+"','"+rs3.getString("pernyataan")+"')";
				    stmt4.executeUpdate(sql);
				    indexCF++;k++;
				}
			    }
			    if (k>1){
				rs4=stmt4.executeQuery("select * from hitungcf where nomor > "+(gantiIf-1)+"");
				rs4.next();
				double a=rs4.getDouble("cf");
				while(rs4.next()){
				    a=cfCombine(a,rs4.getDouble("cf"));
				}
				stmt4.executeUpdate("delete from hitungcf where nomor > "+(gantiIf-1)+"");
				stmt4.executeUpdate("insert into hitungcf values ('"+(gantiIf)+"','"+a+"','"+rs3.getString("pernyataan")+"')");
				indexCF=gantiIf;
				indexCF++;
			    }
			}
		    }  
		}
	    }
	    rs4=stmt4.executeQuery("select * from rule where nomor="+rs.getInt("rule")+"");
	    rs5=stmt5.executeQuery("select cf from hitungcf");
	    double min;
	    rs5.next();
	    min=rs5.getDouble("cf");
	    while(rs5.next()){
		if (min>rs5.getDouble("cf"))
		    min=rs5.getDouble("cf");
	    }
	    double d=0;
	    rs4.next();
	    d=min*rs4.getDouble("ProbThen");
	    stmt5.executeUpdate("insert into probabilitas values ('"+(batasProb+1)+"','"+rs4.getString("then")+"','"+d+"')");
	    batasProb++;
	    stmt4.executeUpdate("update fire set cf="+d+" where nomor="+rs.getInt("nomor")+"");
	    stmt4.executeUpdate("delete from hitungcf");
	}
	rs=stmt.executeQuery("select * from fire");
	rs.next();
	double a=rs.getDouble("cf");
	while (rs.next()){
	    a=cfCombine(a,rs.getDouble("cf"));
	}
	cf=a;
	return cf;
    }
    public static void main(String[] args) throws SQLException {
	try{
	    connectDB();
	    init();
	    ResultSet x;int pilih=0, pilihRule;
	    Scanner angka=new Scanner (System.in);
            while (pilih!=6){
            System.out.println("Pilih salah satu: \n1. Analisa\n2. Hapus rule\n3. Tambah rule\n4. Lihat rule\n5. Update rule\n6. Keluar");
            pilih=angka.nextInt();
            if (pilih==1){
	    System.out.println("Berapa usia anak?");
	    usia=angka.nextInt();
	    if (usia<7){
                addWM("<7");
		addWM("anak");
		fireRule("1");
		pertanyaan("all");
		pertanyaan ("anak");
		inferensi();
		x=stmt.executeQuery("select * from wm");
		while (x.next())
		    System.out.print(x.getString("wm")+"\n");
		System.out.println("\nCF :"+hitungCF());
	    }
	    else if(usia>=7 && usia<18){
                addWM(">=7");
                addWM("<18");
		addWM("remaja");
		fireRule("2");
		pertanyaan("all");
		pertanyaan ("remaja");
		inferensi();
		x=stmt.executeQuery("select * from wm");
		while (x.next())
		    System.out.print(x.getString("wm")+"\n");
		System.out.println("CF :"+hitungCF());
	    }
	    else
		System.out.println("maaf, sistem ini dirancang untuk pasien usia dibawah 18 tahun");
            }
            else if (pilih==2){
                viewRule();
                System.out.println("Pilih nomor rule yang ingin dihapus :");
                pilihRule=angka.nextInt();
                deleteRule(pilihRule);
            }
            else if (pilih==3){
                viewRule();
                addRule();
            }
             else if (pilih==4){
                viewRule();
            }
             else if (pilih==5){
                viewRule();
                System.out.println("Pilih nomor rule yang ingin diupdate :");
                pilihRule=angka.nextInt();
                updateRule(pilihRule);
             }
             else if (pilih<1&&pilih>6)
                 System.out.println("maaf pilihan salah");
            }
        }
	catch (Exception e){
	    System.out.println(e);
	}
    }
}
