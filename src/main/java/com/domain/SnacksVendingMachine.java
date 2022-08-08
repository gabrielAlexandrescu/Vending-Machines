package com.domain;
import com.exceptions.InvalidProductType;
import com.exceptions.NoAdminPrivileges;
import com.exceptions.TooManyProducts;
import com.product_types.Snacks;

public class SnacksVendingMachine extends VendingMachine {
    public SnacksVendingMachine(User user) {
        super(user, false);
    }

    @Override
    public void loadProduct(Product product) throws TooManyProducts, NoAdminPrivileges, InvalidProductType {
        try {
            Snacks valid;
            if (user.isAdmin()) {
                if (!productsInInventory.containsKey(product)) {
                    valid = Snacks.valueOf(product.getName().toUpperCase());
                    productsInInventory.put(product, 0);
                }
            } else {
                throw new NoAdminPrivileges("Loading product unsuccesful");
            }
        } catch (IllegalArgumentException e){
            throw new InvalidProductType("snack");}

        if (productsInInventory.get(product) == 10) {
            throw new TooManyProducts();
        } else {
            productsInInventory.put(product, productsInInventory.get(product) + 1);
        }

    }
}
