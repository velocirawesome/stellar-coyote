package com.velocirawesome.stellarcoyote;

import org.junit.jupiter.api.Test;
import org.tribuo.provenance.SimpleDataSourceProvenance;
import org.tribuo.regression.RegressionFactory;

//@SpringBootTest
public class TribuoServiceTests {

    @Test public void go() {
        
        SimpleDataSourceProvenance provenance = new SimpleDataSourceProvenance("Ledger Regression Dataset", new RegressionFactory());
        
        provenance.toString();
    }

    
}
