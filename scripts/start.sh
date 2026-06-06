#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

ENV_FILE="$PROJECT_ROOT/.env"

check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 1
    fi
    return 0
}

find_available_port() {
    local base_port=$1
    local max_port=$((base_port + 100))
    
    for port in $(seq $base_port $max_port); do
        if check_port $port; then
            echo $port
            return 0
        fi
    done
    echo ""
    return 1
}

update_env_port() {
    local key=$1
    local port=$2
    if grep -q "^${key}=" "$ENV_FILE"; then
        sed -i '' "s/^${key}=.*/${key}=${port}/" "$ENV_FILE"
    else
        echo "${key}=${port}" >> "$ENV_FILE"
    fi
}

echo "========================================"
echo "  手作作品展示分享空间 - 一键启动"
echo "========================================"
echo ""

echo "📋 正在检测端口占用情况..."
echo ""

FRONTEND_DEFAULT=8028
BACKEND_DEFAULT=9028
MYSQL_DEFAULT=3328
REDIS_DEFAULT=6428

FRONTEND_PORT=$(find_available_port $FRONTEND_DEFAULT)
BACKEND_PORT=$(find_available_port $BACKEND_DEFAULT)
MYSQL_PORT=$(find_available_port $MYSQL_DEFAULT)
REDIS_PORT=$(find_available_port $REDIS_DEFAULT)

if [ -z "$FRONTEND_PORT" ]; then
    echo "❌ 无法找到可用的前端端口 ($FRONTEND_DEFAULT-$((FRONTEND_DEFAULT+100)))"
    exit 1
fi

if [ -z "$BACKEND_PORT" ]; then
    echo "❌ 无法找到可用的后端端口 ($BACKEND_DEFAULT-$((BACKEND_DEFAULT+100)))"
    exit 1
fi

if [ -z "$MYSQL_PORT" ]; then
    echo "❌ 无法找到可用的MySQL端口 ($MYSQL_DEFAULT-$((MYSQL_DEFAULT+100)))"
    exit 1
fi

if [ -z "$REDIS_PORT" ]; then
    echo "❌ 无法找到可用的Redis端口 ($REDIS_DEFAULT-$((REDIS_DEFAULT+100)))"
    exit 1
fi

echo "  🌐 前端端口: $FRONTEND_PORT"
echo "  🔧 后端端口: $BACKEND_PORT"
echo "  🗄️  MySQL端口: $MYSQL_PORT"
echo "  📦 Redis端口: $REDIS_PORT"
echo ""

update_env_port "FRONTEND_PORT" "$FRONTEND_PORT"
update_env_port "BACKEND_PORT" "$BACKEND_PORT"
update_env_port "MYSQL_PORT" "$MYSQL_PORT"
update_env_port "REDIS_PORT" "$REDIS_PORT"

echo "🚀 开始构建并启动服务..."
echo ""

cd "$PROJECT_ROOT"
docker compose up --build -d

echo ""
echo "⏳ 等待服务启动完成..."
echo ""

sleep 5

echo ""
echo "========================================"
echo "  ✅ 项目启动成功！"
echo "========================================"
echo ""
echo "  🌐 前端访问地址: http://localhost:$FRONTEND_PORT"
echo "  🔧 后端API地址:  http://localhost:$BACKEND_PORT"
echo "  🗄️  MySQL端口:    $MYSQL_PORT"
echo "  📦 Redis端口:    $REDIS_PORT"
echo ""
echo "  📝 查看日志: docker compose logs -f"
echo "  🛑 停止服务: ./scripts/stop.sh"
echo ""
echo "========================================"
