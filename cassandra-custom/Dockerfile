FROM cassandra:4.1

# Копируем ваш патч-скрипт внутрь
COPY patch-entrypoint.sh /usr/local/bin/patch-entrypoint.sh

ENTRYPOINT [ "/usr/local/bin/patch-entrypoint.sh" ]
CMD [ "cassandra" ]
