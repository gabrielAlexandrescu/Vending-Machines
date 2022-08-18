package com.domain;
import com.exceptions.InvalidProductType;
import com.exceptions.NoAdminPrivileges;
import com.exceptions.TooManyProducts;
import com.product_types.Sodas;

import java.io.IOException;


public class SodasVendingMachine extends  VendingMachine{
    public SodasVendingMachine(Admin admin) {
        super(admin,false);
    }
    @Override
    public void loadProduct(Product product) throws TooManyProducts, NoAdminPrivileges, InvalidProductType {
        try {
            Sodas valid;
            if (user.isAdmin()) {
                if (!productsInInventory.containsKey(product)) {
                    // TODO unused
                    valid = Sodas.valueOf(product.getName().toUpperCase());
                    productsInInventory.put(product, 1);
                } else super.loadProduct(product);
            } else {
                throw new NoAdminPrivileges("Loading product unsuccesful");
            }
        } catch (IllegalArgumentException e){
            throw new InvalidProductType("soda");}
    }
    public void getStatus() throws IOException {
        super.getStatus("sodasOutput.txt");
    }
}
