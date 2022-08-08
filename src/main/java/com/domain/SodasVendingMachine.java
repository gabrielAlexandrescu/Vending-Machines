package com.domain;
import com.exceptions.InvalidProductType;
import com.exceptions.NoAdminPrivileges;
import com.exceptions.TooManyProducts;
import com.product_types.Sodas;


public class SodasVendingMachine extends  VendingMachine{
    public SodasVendingMachine(User user) {
        super(user,false);
    }
    @Override
    public void loadProduct(Product product) throws TooManyProducts, NoAdminPrivileges, InvalidProductType {
        try {
            Sodas valid;
            if (user.isAdmin()) {
                if (!productsInInventory.containsKey(product)) {
                    valid = Sodas.valueOf(product.getName().toUpperCase());
                    productsInInventory.put(product, 0);
                }
            } else {
                throw new NoAdminPrivileges("Loading product unsuccesful");
            }
        } catch (IllegalArgumentException e){
            throw new InvalidProductType("soda");}

        if (productsInInventory.get(product) == 10) {
            throw new TooManyProducts();
        } else {
            productsInInventory.put(product, productsInInventory.get(product) + 1);
        }

    }
}
