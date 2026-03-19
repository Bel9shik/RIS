#!/usr/bin/env zsh
# Хэш слова "aaa1"
HASH="0ad346c93c16e85e2cb117ff1fcfada3"

curl -s -X POST http://localhost:8080/api/hash/crack \
  -H "Content-Type: application/json" \
  -d "{\"hash\":\"${HASH}\",\"maxLength\":5}"
echo
