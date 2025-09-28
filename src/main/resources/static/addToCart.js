//alert("Кнопка нажата!");
 function addToCart(productId) {
      console.log("Adding product with ID: " + productId); // ADDED

fetch('/cart/add/' + productId, {
    method: 'POST'
})
    .then(response => {
             if (response.ok) {
                   alert('Товар добавлен в корзину!');
                 } else {
                  alert('Не удалось добавить товар в корзину.');
              }
   }
   );
}

