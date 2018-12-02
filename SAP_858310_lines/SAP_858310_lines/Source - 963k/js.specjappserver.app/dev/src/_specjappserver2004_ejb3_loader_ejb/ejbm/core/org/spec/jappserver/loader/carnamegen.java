/*
 * Copyright (c) 2001-2007 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID                          Description
 *  ----------  ------------------------    ----------------------------------------------------------------
 *  2004/01/06  Balu Sthanikam, Oracle      Add getMaxNumModels()
 *  2003/03/21  BSthanikam, Oracle          Provide Car descprition.
 *  2003/01/18  BSthanikam, Oracle          Created
 *  2004/02/17  Samuel Kounev, Darmstadt    Corrected static method invocations in main to avoid warnings
 *  2007/10/02  Bernhard Riedhofer, SAP     Integration of loader into SPECjAppServer2007 application
 */
package org.spec.jappserver.loader;

/*
 * Generate Car model names, Manufacturer names etc.
 */
class CarNameGen {
    private static final String CARS[][] = {
            { "Alfa Romeo", "33", "75", "145", "146", "155", "156", "164", "166" },
            { "Audi", "80", "90", "100", "A3", "A4", "A6", "A8", "Cabrio", "Coupe", "Austin", "Metro", "Montego",
                    "Autobianchi", "A112" },
            { "BMW", "Series 3", "Series 5", "Series 7" },
            { "Buick", "Century", "Electra", "LeSabre", "Park Avenue", "Regal" },
            { "Cadillac", "DeVille", "Eldorado", "Seville" },
            { "Chevrolet", "Alero", "Astro", "Beretta", "Blazer", "CK/RV", "Blazer ST", "Caprice", "Cavalier", "Celebrity",
                    "Corsica", "G Series", "Impala 2000", "Lumina", "Malibu", "Metro", "Monte Carlo", "Nova", "Pickup CK RV",
                    "Pickup ST", "Prizm", "Sprint", "Suburban", "Tahoe" },
            { "Chrysler", "300M", "Cirrus", "Concorde", "Intrepid", "LeBaron", "LHS", "Neon", "New Yorker", "Saratoga",
                    "Sebring", "Stratus", "Town & Country", "Vision", "Voyager", "Citroen", "AX", "Berlingo", "BX", "C-15",
                    "GSA", "Jumper", "Jumpy", "Saxo", "Xantia", "XM", "Xsara", "ZX", "Visa" },
            { "Daewoo", "Cielo", "Espero", "Korando", "Lanos", "Leganza", "Matiz", "Musso", "Nexia", "Nubira", "Prince",
                    "Racer", "Daihatsu", "Applause", "Charade", "Cuore", "Feroza", "Gran Move", "Move", "Sirion", "Terios" },
            { "Dodge", "Caravan", "Intrepid", "Neon", "Spirit", "Stratus", "Eagle", "Vision" },
            { "Ferrari", "360", "456M", "575M" },
            { "Fiat", "127", "131", "Brava", "Bravo", "Cinquecento", "Coupe", "Croma", "Ducato", "Duna", "Fiorino", "Marea",
                    "Multipla", "Panda", "Punto", "Regata", "Ritmo", "Seicento", "Tempra", "Tipo", "Ulysse", "Uno" },
            { "Ford", "Escort", "Focus", "Galaxy", "Granada", "Puma", "Scorpio", "Sierra", "Transit", "Aerostar", "Aspire",
                    "Bronco", "Bronco II", "Contour", "Crown Victoria", "Econoline", "Escort", "Expedition", "Explorer",
                    "Festiva", "Mustang", "Pickup F Series", "Probe", "Ranger", "Taurus", "Tempo", "Thunderbird", "Windstar" },
            { "Geo", "Metro", "Prizm" },
            { "GMC", "G Series Van", "Jimmy CK RV", "Jimmy ST", "Pickup CK RV", "Pickup ST", "Safari", "Suburban", "Yukon" },
            { "Honda", "Accord", "Civic", "CRV", "Legend", "Perlude", "Shuttle" },
            { "Isuzu", "Ippon", "Trooper" },
            { "Jeep", "Cherokee", "Grand Cherokee", "Wrangler" },
            { "Kia", "Besta", "Carnival", "Clarus", "Mentor", "Preggio", "Pride", "Sephia", "Shuma", "Sportage", "Lancia",
                    "Dedra", "Delta", "Kappa", "Lybra", "Prizma", "Thema", "Y10" },
            { "Landrover", "Defender", "Discovery" },
            { "Lincoln", "Continental", "Mark 7", "Mark 8", "Navigator", "Town Car" },
            { "Mazda", "121", "323", "626", "Pickup B-series", "Demio", "MPV", "MX-3", "MX-5", "MX-6", "Xedos-9",
                    "Van E-Series", "Premacy" },
            { "Mercedes", "123", "124/E Class", "126", "140/S Class", "168/A Class", "201", "202/C Class", "210/E Class",
                    "Sprinter 900 Ser", "Van Ser 300", "Van Ser 600", "Vito" },
            { "Mercury", "Capri", "Cougar", "Cougar 1999", "Grand Marquiz", "Mystique", "Mountaineer", "Sable", "Topaz",
                    "Tracer", "Villager" },
            { "Mitsubishi", "Carisma", "Champ", "Colt", "Galant", "L 200", "L 300", "Lancer", "Pajero", "Sigma",
                    "Space Gear L400", "Space Runner", "Space Wagon" },
            { "Nissan", "200 SX", "Almera", "Largo", "Maxima", "Micra", "Pickup D ser", "Primera", "Serena", "Sunny",
                    "Terrano II", "Trade", "Vanette" },
            { "Oldsmobile", "Alero", "Bravada", "Cutlass Calais", "Cutlass Cierra", "Cutlass Supreme", "Delta 88", "Intrigue" },
            { "Opel", "Ascona", "Astra", "Campo", "Corsa", "Frontera", "Kadett", "Manta", "Monterey", "Omega", "Tigra",
                    "Vectra" },
            { "Peugeot", "104", "106", "205", "206", "305", "306", "309", "405", "406", "504", "505", "605", "806", "Boxer",
                    "J5", "Partner" },
            { "Plymouth", "Acclaim", "Breeze", "Neon", "Voyager" },
            { "Pontiac", "6000", "Bonneville", "Firebird", "Firefly", "Grand Prix", "J-2000", "Sunbird", "Sunfire", "Tempest" },
            { "Renault", "5", "9", "11", "18", "19", "21", "25", "Clio", "Espace", "Express", "Kangoo", "Laguna", "Megane",
                    "Safrane", "Scenic", "Trafic", "Twingo" },
            { "Rover", "Series 200", "Series 400", "Series 600", "Series 800" },
            { "Saab", "900", "9000", "Seat", "Cordoba", "Ibiza", "Inca", "Leon", "Malaga", "Toledo" },
            { "Smart", "fortwo", "forfour", "Roadster", "Roadster Coupe"},
            { "Subaru", "E-10", "Forester", "Impreza", "Justy", "Legacy", "Series L" },
            { "Suzuki", "Alto", "Baleno", "G-Vitara", "SJ Series", "Swift", "Vitara" },
            { "Toyota", "4-Runner", "Avensis", "Carina", "Celica", "Corolla", "Hilux", "Previa", "RAV-4", "Starlet", "Tarago",
                    "Yaris" },
            { "Volkswagen", "Beetle", "Bora", "Caddy 83", "Caddy 96", "Golf", "Jetta", "LT", "New Beetle", "Passat", "Polo",
                    "Santana", "Sharan", "Transporter", "Vento" },
            { "Volvo", "Series 40", "Series 70", "Series 80", "Series 90", "Series 200", "Series 300", "Series 700", "850",
                    "Series 900" } };
    private static final int NUM_MODELS; // total number of models in above array.
    static {
        int total = 0;
        for (int i = 0; i < CARS.length; i++) {
            for (int j = 1; j < CARS[i].length; j++) {
                total++;
            }
        }
        NUM_MODELS = total;
    }
    private static final String YEARS[] = { "2003", "2002", "2001", "2000", "1999", "1998", "1997", "1996", "1995", "1994",
            "1993", "1992", "1991", "1990", "1989", "1988", "1987", "1986", "1985", "1984", "1983", "1982", "1980", "1979",
            "1978" };
    private static final String ENGINES[] = { "1.8L 4Cyl", "2.0L V6", "3.4L V8", "4.0L V8" };
    private static final String COLOR_NAMES[] = { "Red", "White", "Black", "Blue", "Yellow", "Grey", "Green" };
    private final RandNum r;

    CarNameGen(long seed) {
        r = new RandNum(seed);
    }

    // Get A String which has a desc. about the car.
    String getDesc() {
        final StringBuffer sb = new StringBuffer();
        final int n = r.random(0, ENGINES.length - 1);
        sb.append(ENGINES[n]);
        final int n2 = r.random(0, COLOR_NAMES.length - 1);
        sb.append(", ");
        sb.append(COLOR_NAMES[n]);
        if (n % 2 == 0) {
            sb.append(", Interior=Leather");
        } else {
            sb.append(", Interior=Fabric");
        }
        if (n2 % 2 == 0) {
            sb.append(", Sunroof=Yes");
        } else {
            sb.append(", Sunroof=No");
        }
        return sb.toString();
    }

    // Maximum possible number of cars(combinations of Mfr, model and year
    int getMaxNumModels() {
        return NUM_MODELS * YEARS.length;
    }

    // get the idx'th car in the array. idx starts from 1.
    // includes a model year too. e.g. 2003 BMW 7Series.
    String getModelByIdx(int idx) {
        // get the year
        if ((idx <= 0) || (idx > NUM_MODELS * YEARS.length)) {
            throw new IllegalArgumentException("Ilegal index specified for Car name:" + idx);
        }
        int yearIdx = idx / NUM_MODELS;
        if (yearIdx == YEARS.length) {
            yearIdx = YEARS.length - 1; // take care of the last model, last year
        }
        if (idx % NUM_MODELS == 0) {
            idx = NUM_MODELS;
        } else {
            idx = idx % NUM_MODELS;
        }
        int cur = 1;
        int i = 0;
        for (; i < CARS.length; i++) {
            if (CARS[i].length + cur - 1 > idx) {
                break;
            } else {
                cur += CARS[i].length - 1;
            }
        }
        for (int j = 1; j < CARS[i].length; j++) {
            if (cur == idx) {
                return YEARS[yearIdx] + " " + CARS[i][0] + " " + CARS[i][j];
            }
            cur++;
        }
        throw new RuntimeException("Error  :   Cant find " + idx + " th car");
    }

    // get idx'th Mfr. idx starts from 0
    static String getMfr(int idx) {
        if ((idx < 0) || (idx >= CARS.length)) {
            throw new IllegalArgumentException("Illegal index specified for getMfr:" + idx);
        }
        return CARS[idx][0];
    }

    static int getNumMfrs() {
        return CARS.length;
    }
}
