function checkout() {
    console.log('Checkout function called!'); // Добавьте этот лог для проверки
    fetch('/checkout', {
        method: 'POST',
        // Другие параметры запроса (например, данные корзины)
        // В данном примере мы не передаем данные корзины,
        // но в реальном приложении их нужно будет передать.
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            // Здесь нужно передать данные корзины, например:
            // cartItems: cartItems
        })
    })
        .then(response => {
            // Обрабатываем ответ от сервера
            if (response.ok) {
                // Заказ успешно оформлен
                alert('Заказ успешно оформлен!');
                // Перенаправляем пользователя на страницу подтверждения заказа
                window.location.href = '/order-success';
            } else {
                // Произошла ошибка при оформлении заказа
                alert('Произошла ошибка при оформлении заказа.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Произошла ошибка при оформлении заказа.');
        });
}