package com.domain;

import com.exceptions.InvalidProductType;
import com.exceptions.NoAdminPrivileges;
import com.exceptions.TooManyProducts;
import com.product_types.Sodas;

import static com.utils.Utils.checkProductType;

import java.io.IOException;


public class SodasVendingMachine extends VendingMachine {
    public SodasVendingMachine(Admin admin) {
        super(admin, false);
    }

    @Override
    public void loadProduct(Product product) throws TooManyProducts, NoAdminPrivileges, InvalidProductType {

        if (user.isAdmin()) {
            if (!productsInInventory.containsKey(product)) {
                if (product.getName() == null) {
                    throw new NullPointerException("The product must have a name!");
                }
                if (checkProductType(Sodas.class, product.getName().toUpperCase())) {
                    productsInInventory.put(product, 1);
                } else throw new InvalidProductType("soda");
            } else super.loadProduct(product);
        } else {
            throw new NoAdminPrivileges("Loading product unsuccesful");
        }

    }

    public void getStatus() throws IOException {
        super.getStatus("sodasOutput.txt");
    }
}
