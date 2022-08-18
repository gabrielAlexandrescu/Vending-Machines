package com.domain;

import com.exceptions.InvalidProductType;
import com.exceptions.NoAdminPrivileges;

import com.exceptions.TooManyProducts;
import com.product_types.Utilities;

import static com.utils.Utils.checkProductType;

import java.io.IOException;

public class UtilitiesVendingMachine extends VendingMachine {
    public UtilitiesVendingMachine(Admin admin) {
        super(admin, true);
    }

    @Override
    public void loadProduct(Product product) throws TooManyProducts, NoAdminPrivileges, InvalidProductType {


        if (user.isAdmin()) {
            if (product.getName() == null) {
                throw new NullPointerException("The product must have a name!");
            }
            if (!productsInInventory.containsKey(product)) {

                if (checkProductType(Utilities.class, product.getName().toUpperCase())) {
                    productsInInventory.put(product, 1);
                } else throw new InvalidProductType("utility");
            } else {
                super.loadProduct(product);
            }
        } else {
            throw new NoAdminPrivileges("Loading product unsuccesful");
        }
    }


    public void getStatus() throws IOException {
        super.getStatus("utilitiesOutput.txt");
    }
}
