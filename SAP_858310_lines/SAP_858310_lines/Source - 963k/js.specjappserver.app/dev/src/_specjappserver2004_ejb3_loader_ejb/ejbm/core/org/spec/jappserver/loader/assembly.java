/*
 * Copyright (c) 2001-2007 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  -----------------------   ---------------------------------------------------------------
 *  2007/10/02  Bernhard Riedhofer, SAP   Integration of loader into SPECjAppServer2007 application
 */
package org.spec.jappserver.loader;

import java.util.HashMap;
import java.util.Map;

/*
 * Generates random data for an assembly.
 */
final class Assembly extends Part {

    private static final String ASSEMBLY_STRING = "MITEM";

    static final class RandomGenerator {

        private final RandNum r;
        private final RandNum costR;
        private final CarNameGen carNames;
        private int assemblyNumber;
        
        RandomGenerator(final long seed, int startAssemblyNumber) {
            r = new RandNum(seed);
            costR = new RandNum(r.nextLong());
            carNames = new CarNameGen(r.nextLong());
            assemblyNumber = startAssemblyNumber;
        }
        
        double createCost() {
            return costR.drandom(6000.00, 30000.00);
        }
        
        Assembly createAssembly(final int scale) {
            double cost = createCost();
            Assembly result = new Assembly(r, scale, assemblyNumber, carNames, cost);
            assemblyNumber++;
            return result;
        }
    }

    static Map<Integer, Double> getAssemblyPrices(int numAssemblies,
            int numAssembliesPerBlock, SeedGenerator seedGen) {
        Map<Integer, Double> result = new HashMap<Integer, Double>(numAssemblies);
        int assemblyBlocks = numAssemblies/numAssembliesPerBlock;
        int assemblyId = 1;
        for (int i = 0; i < assemblyBlocks; i++) {
            final Assembly.RandomGenerator assemblyGen = new Assembly.RandomGenerator(seedGen
                    .getAssemblySeed(i), 0);
            for (int j = 0; j < numAssembliesPerBlock; j++) {
                result.put(assemblyId, Assembly.getPrice(assemblyGen.createCost()));
                assemblyId++;
            }
        }
        return result;
    }
    
    static String getAssemblyId(final int scale, final int id) {
        return getPartId(scale, Assembly.ASSEMBLY_STRING, id);
    }
    
    int pType;

    public Assembly(final RandNum r, final int scale, final int partNo, final CarNameGen carNames, double cost) {
        super(r);
        pId = getAssemblyId(scale, partNo);
        pType = r.random(1, 4);
        int carIdx = (partNo % carNames.getMaxNumModels()) + 1;
        pName = carNames.getModelByIdx(carIdx);
        pDesc = carNames.getDesc();
        pCost = cost;
    }

    @Override
    int getType() {
        return pType;
    }

    static double getPrice(double cost) {
        return 1.5 * cost;
    }
    
    @Override
    double getPrice() {
        return getPrice(pCost);
    }
}
