/*
 * Copyright (c) 2001-2007 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  -----------------------   ---------------------------------------------------------------
 *  2007/10/02  Bernhard Riedhofer, SAP   Created, integration of loader into SPECjAppServer2007 application
 */
package org.spec.jappserver.loader;

/*
 * There is data which is generated for a table and which is reused for other tables.
 * Previously this was realized using streams which are written to the file system.
 *
 * Now tables can be loaded independantly:
 * The random data is deterministically (re-)generated at the time when it is needed
 * by using the same seed of a pseudo random number generator (PRNG).
 */
class SeedGenerator {

    private final long rootSeed;
    private final long customerCreditSeed;
    private final long assemblySeed;
    private final long assemblyPriceSeed;
    private final long componentSeed;
    private final long siteAddressSeed;
    private final long supplierAddressSeed;

    private static long getSeed(long seed, int block)
    {
        RandNum r = new RandNum(seed);
        for (int i = 0; i < block; i++)
        {
            r.nextLong();
        }
        return r.nextLong();
    }
    
    public SeedGenerator(long rootSeed) {
        this.rootSeed = rootSeed;
        RandNum r = new RandNum(rootSeed);
        customerCreditSeed = r.nextLong();
        assemblySeed = r.nextLong();
        assemblyPriceSeed = r.nextLong();
        componentSeed = r.nextLong();
        siteAddressSeed = r.nextLong();
        supplierAddressSeed = r.nextLong();
    }
    
    public long getRootSeed() {
        return rootSeed;
    }

    public long getCustomerCreditSeed(int block)
    {
        return getSeed(customerCreditSeed, block);
    }

    public long getAssemblySeed(int block)
    {
        return getSeed(assemblySeed, block);
    }

    public long getAssemblyPriceSeed(int block)
    {
        return getSeed(assemblyPriceSeed, block);
    }

    public long getComponentSeed(int block)
    {
        return getSeed(componentSeed, block);
    }

    public long getSiteAddressSeed()
    {
        return siteAddressSeed;
    }

    public long getSupplierAddressSeed()
    {
        return supplierAddressSeed;
    }
}
