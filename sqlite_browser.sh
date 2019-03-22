docker run -it --rm \
    -p 8081:8080 \
    -v $(pwd)/:/data/:ro \
    -e SQLITE_DATABASE=migration.db \
    coleifer/sqlite-web