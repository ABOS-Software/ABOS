/*
 * Copyright (c) Patrick Magauran 2018.
 *   Licensed under the AGPLv3. All conditions of said license apply.
 *       This file is part of ABOS.
 *
 *       ABOS is free software: you can redistribute it and/or modify
 *       it under the terms of the GNU Affero General Public License as published by
 *       the Free Software Foundation, either version 3 of the License, or
 *       (at your option) any later version.
 *
 *       ABOS is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU Affero General Public License for more details.
 *
 *       You should have received a copy of the GNU Affero General Public License
 *       along with ABOS.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.math.BigDecimal;

public class formattedProduct {
    public final int productKey;

    public final String productID;
    public final String productName;
    public final String productSize;
    public final BigDecimal productUnitPrice;
    public final String productCategory;
    public final int orderedQuantity;
    public final BigDecimal extendedCost;

    public formattedProduct(int productKey, String productID, String productName, String productSize, BigDecimal productUnitPrice, String productCategory, int orderedQuantity, BigDecimal extendedCost) {
        this.productKey = productKey;
        this.productID = productID;
        this.productName = productName;
        this.productSize = productSize;
        this.productUnitPrice = productUnitPrice;
        this.productCategory = productCategory;
        this.orderedQuantity = orderedQuantity;
        this.extendedCost = extendedCost;
    }
}