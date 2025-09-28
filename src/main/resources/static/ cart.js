console.log('cart.js loaded');
document.addEventListener('DOMContentLoaded', function() { // Подождем загрузки DOM

    const checkoutButton = document.getElementById('checkout-btn');

    if (checkoutButton) { // Проверим, что кнопка существует
        checkoutButton.addEventListener('click', function(event) {
            event.preventDefault(); // Предотвратим стандартное поведение (отправку формы)

            console.log('Кнопка "Оформить заказ" нажата!'); // Лог (обязательно)

            // Отправка запроса на сервер
            fetch('/checkout', { // Замените '/checkout' на ваш URL
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json', // Если сервер ожидает JSON
                    // 'X-CSRF-TOKEN': // Добавьте CSRF токен, если он нужен
                },
            })
                .then(response => {
                    if (response.ok) {
                        console.log('Заказ успешно создан!');
                        // Перенаправление на страницу подтверждения или другую страницу
                        window.location.href = '/order/confirmation'; // Замените на ваш URL
                    } else {
                        console.error('Ошибка при создании заказа:', response.status);
                        alert('Ошибка при оформлении заказа. Пожалуйста, попробуйте еще раз.');
                    }
                })
                .catch(error => {
                    console.error('Ошибка при запросе:', error);
                    alert('Произошла ошибка. Пожалуйста, попробуйте позже.');
                });
        });
    } else {
        console.warn('Кнопка "Оформить заказ" не найдена.'); // Лог, если кнопка не найдена
    }
});