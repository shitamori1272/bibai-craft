#!/bin/zsh
set -eu
set -o pipefail

root=${0:A:h}
data="$root/server-data"
mkdir -p "$data"
cd "$data"

if ! grep -Eq '^eula=true$' eula.txt 2>/dev/null; then
  print 'Minecraft EULA: https://www.minecraft.net/ja-jp/eula'
  read -r 'answer?内容に同意して起動する場合は yes と入力してください: '
  [[ "$answer" == yes ]] || exit 1
  print 'eula=true' > eula.txt
fi

# Use the Homebrew JDK even when macOS's java launcher has no registered JDK.
if [[ -n "${JAVA_HOME:-}" ]]; then
  java_bin="$JAVA_HOME/bin/java"
elif [[ -x /opt/homebrew/opt/openjdk/bin/java ]]; then
  java_bin=/opt/homebrew/opt/openjdk/bin/java
else
  java_bin=java
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
version=26.3
sha1=33680f5f2ac32864d6d7cf5e56a705fdb3e05f4c
jar="minecraft_server.$version.jar"
if [[ ! -f "$jar" ]]; then
  print "Minecraft Java Edition $version を取得しています…"
  curl --fail --silent --show-error --location --retry 2 --connect-timeout 10 \
    "https://piston-data.mojang.com/v1/objects/$sha1/server.jar" -o "$tmp/server.jar"
  (cd "$tmp" && print -r -- "$sha1  server.jar" | shasum -a 1 -c -)
  mv "$tmp/server.jar" "$jar"
fi

cat "$root/server/server.properties" > "$tmp/server.properties"
print -r -- "server-ip=$lan_ip" >> "$tmp/server.properties"
mv "$tmp/server.properties" server.properties
rm -rf "$tmp"
trap - EXIT

print "Java Edition $version | 接続先 $lan_ip:25565"
print 'ログに Done が出たら参加できます。終了するには stop を入力してください。'
exec "$java_bin" -Xms512M -Xmx2G -jar "$jar" nogui
