package com.domain;
import com.exceptions.InvalidProductType;
import com.exceptions.NoAdminPrivileges;
import com.exceptions.TooManyProducts;
import com.product_types.Utilities;

import java.io.IOException;

public class UtilitiesVendingMachine extends  VendingMachine{
    public UtilitiesVendingMachine(Admin admin) {
        super(admin,true);
    }
    @Override
    public void loadProduct(Product product) throws TooManyProducts, NoAdminPrivileges, InvalidProductType {
        try {
            Utilities valid;
            if (user.isAdmin()) {
                if (!productsInInventory.containsKey(product)) {
                    valid = Utilities.valueOf(product.getName().toUpperCase());
                    productsInInventory.put(product, 1);
                } else super.loadProduct(product);
            } else {
                throw new NoAdminPrivileges("Loading product unsuccesful");
            }
        } catch (IllegalArgumentException e){
            throw new InvalidProductType("utility");}
    }
    public void getStatus() throws IOException {
        super.getStatus("utilitiesOutput.txt");
    }
}
