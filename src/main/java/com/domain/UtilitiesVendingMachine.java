package com.domain;
import com.exceptions.InvalidProductType;
import com.exceptions.NoAdminPrivileges;
import com.exceptions.TooManyProducts;
import com.product_types.Utilities;

public class UtilitiesVendingMachine extends  VendingMachine{
    public UtilitiesVendingMachine(User user) {
        super(user,true);
    }
    @Override
    public void loadProduct(Product product) throws TooManyProducts, NoAdminPrivileges, InvalidProductType {
        try {
            Utilities valid;
            if (user.isAdmin()) {
                if (!productsInInventory.containsKey(product)) {
                    valid = Utilities.valueOf(product.getName().toUpperCase());
                    productsInInventory.put(product, 0);
                }
            } else {
                throw new NoAdminPrivileges("Loading product unsuccesful");
            }
        } catch (IllegalArgumentException e){
            throw new InvalidProductType("utility");}

        if (productsInInventory.get(product) == 10) {
            throw new TooManyProducts();
        } else {
            productsInInventory.put(product, productsInInventory.get(product) + 1);
        }

    }
}
