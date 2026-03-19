#!/usr/bin/env zsh
# Использование: ./checkStatus.sh <requestId>
# Пример: ./checkStatus.sh f295efe1-8d94-475a-ac3f-ddcee7376e2f
if [[ -z "$1" ]]; then
  echo "Укажи requestId: $0 <requestId>" >&2
  exit 1
fi
curl -s "http://localhost:8080/api/hash/status?requestId=$1"
echo
