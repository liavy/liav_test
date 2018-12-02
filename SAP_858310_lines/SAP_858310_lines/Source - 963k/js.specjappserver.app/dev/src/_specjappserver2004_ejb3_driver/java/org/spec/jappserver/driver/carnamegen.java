/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 *  History:
 *  Date        ID                        Description
 *  ----------  ------------------------  ----------------------------------------------
 *  2003/01/18  Balu, Oracle              Created
 *  2004/02/17  Samuel Kounev, Darmstadt  Corrected static method invocations in main to avoid warnings
 *
 *
 * Generate Car model names, Manufacturer names etc.
 * @author Balu Sthanikam
 */

package org.spec.jappserver.driver;


/**
 * Provides all possible car name mappings.
 */
public class CarNameGen {

static final String cars[][] = {

{"Alfa_Romeo","33","75","145","146","155","156","164","166"},
{"Audi","80","90","100","A3","A4","A6","A8","Cabrio","Coupe","Austin","Metro","Montego","Autobianchi","A112"},
{"BMW","Series_3","Series_5","Series_7"},
{"Buick","Century","Electra","LeSabre","Park_Avenue","Regal"},
{"Cadillac","DeVille","Eldorado","Seville"},
{"Chevrolet","Alero","Astro","Beretta","Blazer","CK/RV","Blazer_ST","Caprice","Cavalier","Celebrity","Corsica","G_Series","Impala_2000","Lumina","Malibu","Metro","Monte_Carlo","Nova","Pickup_CK_RV","Pickup_ST","Prizm","Sprint","Suburban","Tahoe"},
{"Chrysler","300M","Cirrus","Concorde","Intrepid","LeBaron","LHS","Neon","New_Yorker","Saratoga","Sebring","Stratus","Town_&_Country","Vision","Voyager","Citroen","AX","Berlingo","BX","C-15","GSA","Jumper","Jumpy","Saxo","Xantia","XM","Xsara","ZX","Visa"},
{"Daewoo","Cielo","Espero","Korando","Lanos","Leganza","Matiz","Musso","Nexia","Nubira","Prince","Racer","Daihatsu","Applause","Charade","Cuore","Feroza","Gran_Move","Move","Sirion","Terios"},
{"Dodge","Caravan","Intrepid","Neon","Spirit","Stratus","Eagle","Vision"},
{"Ferrari","360","456M","575M"},
{"Fiat","127","131","Brava","Bravo","Cinquecento","Coupe","Croma","Ducato","Duna","Fiorino","Marea","Multipla","Panda","Punto","Regata","Ritmo","Seicento","Tempra","Tipo","Ulysse","Uno"},
{"Ford","Escort","Focus","Galaxy","Granada","Puma","Scorpio","Sierra","Transit","Aerostar","Aspire","Bronco","Bronco_II","Contour","Crown_Victoria","Econoline","Escort","Expedition","Explorer","Festiva","Mustang","Pickup_F_Series","Probe","Ranger","Taurus","Tempo","Thunderbird","Windstar"},
{"Geo","Metro","Prizm"},
{"GMC","G_Series_Van","Jimmy_CK_RV","Jimmy_ST","Pickup_CK_RV","Pickup_ST","Safari","Suburban","Yukon"},
{"Honda","Accord","Civic","CRV","Legend","Perlude","Shuttle"},
{"Isuzu","Ippon","Trooper"},
{"Jeep","Cherokee","Grand_Cherokee","Wrangler"},
{"Kia","Besta","Carnival","Clarus","Mentor","Preggio","Pride","Sephia","Shuma","Sportage","Lancia","Dedra","Delta","Kappa","Lybra","Prizma","Thema","Y10"},
{"Landrover","Defender","Discovery"},
{"Lincoln","Continental","Mark_7","Mark_8","Navigator","Town_Car"},
{"Mazda","121","323","626","Pickup_B-series","Demio","MPV","MX-3","MX-5","MX-6","Xedos-9","Van_E-Series","Premacy"},
{"Mercedes","123","124/E_Class","126","140/S_Class","168/A_Class","201","202/C_Class","210/E_Class","Sprinter_900_Ser","Van_Ser_300","Van_Ser_600","Vito"},
{"Mercury","Capri","Cougar","Cougar_1999","Grand_Marquiz","Mystique","Mountaineer","Sable","Topaz","Tracer","Villager"},
{"Mitsubishi","Carisma","Champ","Colt","Galant","L_200","L_300","Lancer","Pajero","Sigma","Space_Gear_L400","Space_Runner","Space_Wagon"},
{"Nissan","200_SX","Almera","Largo","Maxima","Micra","Pickup_D_ser","Primera","Serena","Sunny","Terrano_II","Trade","Vanette"},
{"Oldsmobile","Alero","Bravada","Cutlass_Calais","Cutlass_Cierra","Cutlass_Supreme","Delta_88","Intrigue"},
{"Opel","Ascona","Astra","Campo","Corsa","Frontera","Kadett","Manta","Monterey","Omega","Tigra","Vectra"},
{"Peugeot","104","106","205","206","305","306","309","405","406","504","505","605","806","Boxer","J5","Partner"},
{"Plymouth","Acclaim","Breeze","Neon","Voyager"},
{"Pontiac","6000","Bonneville","Firebird","Firefly","Grand_Prix","J-2000","Sunbird","Sunfire","Tempest"},
{"Renault","5","9","11","18","19","21","25","Clio","Espace","Express","Kangoo","Laguna","Megane","Safrane","Scenic","Trafic","Twingo"},
{"Rover","Series_200","Series_400","Series_600","Series_800"},
{"Saab","900","9000","Seat","Cordoba","Ibiza","Inca","Leon","Malaga","Toledo"},
{"Subaru","E-10","Forester","Impreza","Justy","Legacy","Series_L"},
{"Suzuki","Alto","Baleno","G-Vitara","SJ_Series","Swift","Vitara"},
{"Toyota","4-Runner","Avensis","Carina","Celica","Corolla","Hilux","Previa","RAV-4","Starlet","Tarago","Yaris"},
{"Volkswagen","Beetle","Bora","Caddy_83","Caddy_96","Golf","Jetta","LT","New_Beetle","Passat","Polo","Santana","Sharan","Transporter","Vento"},
{"Volvo","Series_40","Series_70","Series_80","Series_90","Series_200","Series_300","Series_700","850","Series_900"}
};

	int numModels; // total number of models in above array.
	int dbFieldWidth = 35; // size of char column in db

	static final String years[] = {
			"2003", "2002", "2001", "2000", "1999", "1998", "1997", "1996", "1995",
			"1994", "1993", "1992", "1991", "1990", "1989", "1988", "1987", "1986",
			"1985", "1984", "1983", "1982","1980", "1979", "1978" };

    /**
     * Constructs the car name generator.
     * @param dbFieldWidth
     */
	public CarNameGen(int dbFieldWidth) {
		int total = 0;

		for(int i = 0; i < cars.length; i++) {
			for(int j = 1; j < cars[i].length; j++) {
				total++;
			}
		}
		this.numModels = total;

		this.dbFieldWidth = dbFieldWidth;
	}

    /**
     * Get the idx'th car in the array. Idx starts from 1.
     * appends a model year too. e.g. BMW_7Series_2003.
     * @param idx The index
     * @return The model name and year
     * @throws Exception An error occurred
     */
	public String getModelByIdx(int idx) throws Exception {

		// get the year
		if ( (idx <= 0) || (idx > getNumModels() * years.length) )
			throw new Exception("Ilegal index specified for Car name:" + idx );

		int yr_idx = idx / getNumModels(); 
		if(yr_idx == years.length)
			yr_idx = years.length - 1; // take care of the last model, last year
		if(idx % getNumModels() == 0)
			idx = getNumModels();
		else
			idx = idx % getNumModels();

		int cur = 1;
		int i = 0;
		for(; i < cars.length; i++) {
			if (cars[i].length + cur - 1 > idx)
				break;
			else
				cur += cars[i].length - 1;
		}

		for(int j=1; j<cars[i].length; j++) {
			if (cur == idx)
			{
				return addTrailingBlanks(new String(cars[i][0] + "_" + cars[i][j] + "_" + years[yr_idx]));
			}
			cur++;
		}
		
		throw new Exception("Error  :   Cant find " + idx + " th car");
	}

    /**
     * Provides the total number of models available.
     * @return The number of models
     */
	public int getNumModels() {
		return this.numModels;
	}

	/**
     * Pads the string with trailing blanks.
     * @param s The string
     * @return The new paded string
     */
    public String addTrailingBlanks(String s) {
		if(s.length() == this.dbFieldWidth)
			return s;

		StringBuffer sb = new StringBuffer(s);
		int diff = this.dbFieldWidth - s.length();

		for(int i = 0; i < diff; i++) 
			sb.append(' ');
		  
		return new String(sb);
	}

    /**
     * get idx'th Mfr. Idx starting from 0.
     * @param idx The index
     * @return The manufacturer
     * @throws Exception Error occurred
     */
	public static String getMfr(int idx) throws Exception {

		if( (idx < 0) || (idx >= cars.length) )
			throw new Exception("Illegal index specified for getMfr:" + idx);

		return new String(cars[idx][0]);
	}

    /**
     * Get the number of available manufacturers.
     * @return The number of available manufacturers
     */
	public static int getNumMfrs() {
		return cars.length;
	}

	/**
     * Main method for testing CarNameGen.
     * @param args Command line arguments
     * @throws Exception Any errors occurring during the run.
     */
    public static void main(String args[]) throws Exception {
		CarNameGen carNameGen = new CarNameGen(35);
		RandNum rd = new RandNum();

		for(int i = 0; i < 10; i++)
			System.out.println(rd.random(0, CarNameGen.getNumMfrs()));

		System.out.println("number pf cars = " + carNameGen.getNumModels());
		System.out.println("1st mfr = " + CarNameGen.getMfr(0));
		System.out.println("30st mfr = " + CarNameGen.getMfr(30));
		System.out.println("num mfr = " + CarNameGen.getNumMfrs());
		System.out.println("fifth car is = " + carNameGen.getModelByIdx(5) +
			"len = " + carNameGen.getModelByIdx(5).length());
		System.out.println("396 car is = " + carNameGen.getModelByIdx(396));
		System.out.println("792 car is = " + carNameGen.getModelByIdx(792));
		System.out.println("1st mfr is = " + CarNameGen.getMfr(0));
		System.out.println("30st mfr is = " + CarNameGen.getMfr(30));
		System.out.println("20 mfr is = " + CarNameGen.getMfr(20));
		System.out.println("10 car is = " + carNameGen.getModelByIdx(10) +
			"len = " + carNameGen.getModelByIdx(10).length());

		for(int i = 1; i < 700; i++) {
			System.out.println(i + " th car is :" + carNameGen.getModelByIdx(i));
		}
	}
}
