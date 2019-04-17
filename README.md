Запускаем Application. Вызываем http://localhost:8080/. Видим welcome.  
Смотрим в логи свой `RemoteAddr:`. Добавляем в blacklist.txt его.   
Вызываем http://localhost:8080/. Видим `Bad IP`. 
В Application метод watch реализует Watch Service API.   
При изменении файла дергается метод readTxtFile(), который считывает ип из 
 файла и ложит их в Set. Фильтр при запросе проверяет наличие ип в запрете.
 Можно к примеру каждый раз при запросе, в фильтре ходить в файл как в базу.
 Тогда ип можно хранить как число.
  
  absolute.path.directory.blacklist заполнить
