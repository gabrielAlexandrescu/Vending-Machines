package com.domain;

import com.exceptions.InvalidProductType;
import com.exceptions.NoAdminPrivileges;
import com.exceptions.TooManyProducts;
import com.product_types.Snacks;

public class SnacksVendingMachine extends VendingMachine {
    public SnacksVendingMachine(Admin admin) {
        super(admin, false);
    }

    @Override
    public void loadProduct(Product product) throws TooManyProducts, NoAdminPrivileges, InvalidProductType {
        try {
            Snacks valid;
            if (user.isAdmin()) {
                if (!productsInInventory.containsKey(product)) {
                    valid = Snacks.valueOf(product.getName().toUpperCase());
                    productsInInventory.put(product, 1);
                } else super.loadProduct(product);
            } else {
                throw new NoAdminPrivileges("Loading product unsuccesful");
            }
        } catch (IllegalArgumentException e) {
            throw new InvalidProductType("snack");
        }
    }
}
