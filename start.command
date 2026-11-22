#!/bin/zsh
set -eu
set -o pipefail

root=${0:A:h}
data="$root/server-data"
mkdir -p "$data"
cd "$data"

# Use the Homebrew JDK even when macOS's java launcher has no registered JDK.
if [[ -n "${JAVA_HOME:-}" ]]; then
  java_bin="$JAVA_HOME/bin/java"
elif [[ -x /opt/homebrew/opt/openjdk/bin/java ]]; then
  java_bin=/opt/homebrew/opt/openjdk/bin/java
else
  java_bin=java
fi
mode=${1:-local}
if (( $# > 1 )) || [[ "$mode" != local && "$mode" != preview && "$mode" != publish ]]; then
  print -u2 '使い方: ./start.command [local|preview|publish]'
  exit 1
fi
if [[ "$mode" == publish ]]; then
  exec "$java_bin" "$root/server/World.java" publish
fi

if ! grep -Eq '^eula=true$' eula.txt 2>/dev/null; then
  print 'Minecraft EULA: https://www.minecraft.net/ja-jp/eula'
  read -r 'answer?内容に同意して起動する場合は yes と入力してください: '
  [[ "$answer" == yes ]] || exit 1
  print 'eula=true' > eula.txt
fi

"$java_bin" -version

# Bind only to a LAN IPv4 address, including on Macs with a VPN.
lan_ip=${BIBAI_LAN_IP:-$(/sbin/ifconfig | awk '
  /^[^[:space:]]/ { ethernet = ($1 ~ /^en[0-9]+:$/) }
  ethernet && $1 == "inet" && ($2 ~ /^10\./ || $2 ~ /^192\.168\./ || $2 ~ /^172\.(1[6-9]|2[0-9]|3[01])\./) {
    if (!address) address = $2
  }
  END { print address }')}
if [[ -z "$lan_ip" ]]; then
  print -u2 'LANのIPv4アドレスを取得できません。BIBAI_LAN_IP にこのMacのLANアドレスを指定してください。'
  exit 1
fi

tmp=$(mktemp -d "$data/.setup.XXXXXX")
trap 'rm -rf "$tmp"' EXIT
# Paper のビルドを更新するときは build と sha256 を両方書き換える。
# 一覧: https://fill.papermc.io/v3/projects/paper/versions/26.3/builds
version=26.3
build=257
sha256=64a5bd44910cb982b2648e9a35036cc95a6de4d561e15812aecf5411f84f7b01
jar="paper-$version.jar"
if ! print -r -- "$sha256  $jar" | shasum -a 256 -c - >/dev/null 2>&1; then
  print "Paper $version (build $build) を取得しています…"
  curl --fail --silent --show-error --location --retry 2 --connect-timeout 10 \
    "https://fill-data.papermc.io/v1/objects/$sha256/paper-$version-$build.jar" -o "$tmp/server.jar"
  (cd "$tmp" && print -r -- "$sha256  server.jar" | shasum -a 256 -c -)
  mv "$tmp/server.jar" "$jar"
fi

cat "$root/server/server.properties" > "$tmp/server.properties"
print -r -- "server-ip=$lan_ip" >> "$tmp/server.properties"
mv "$tmp/server.properties" server.properties
rm -rf "$tmp"
trap - EXIT

print "Paper $version (build $build) | 接続先 $lan_ip:25565"
print 'ログに Done が出たら参加できます。終了するには stop を入力してください。'
exec "$java_bin" "$root/server/World.java" "$mode"
