package com.domain;

import com.exceptions.InvalidProductType;
import com.exceptions.NoAdminPrivileges;
import com.exceptions.TooManyProducts;
import com.product_types.Snacks;


import static com.utils.Utils.checkProductType;

import java.io.IOException;

public class SnacksVendingMachine extends VendingMachine {
    public SnacksVendingMachine(Admin admin) {
        super(admin, false);
    }

    @Override
    public void loadProduct(Product product) throws TooManyProducts, NoAdminPrivileges, InvalidProductType {

        if (user.isAdmin()) {
            if (product.getName() == null) {
                throw new NullPointerException("The product must have a name!");
            }
            if (!productsInInventory.containsKey(product)) {
                if (checkProductType(Snacks.class, product.getName().toUpperCase())) {
                    productsInInventory.put(product, 1);
                } else throw new InvalidProductType("snack");
            } else super.loadProduct(product);
        } else {
            throw new NoAdminPrivileges("Loading product unsuccesful");
        }

    }

    public void getStatus() throws IOException {
        super.getStatus("snacksOutput.txt");
    }
}
