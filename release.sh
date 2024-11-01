#!/bin/bash
set -e
function print_usage(){
    cat << EOF
Usage: $(basename $0)

Note that this scripts that the .deb file was already created!
EOF
}
function abort(){
    echo "Error: $@" >&2
    exit 1
}

function ask_y(){
    local confirm="initial"
    local prefix=""
    local suffix=" [Yn]: "
    while ! [ -z "${confirm}" ] && ! [[ "${confirm}" =~ ^[YyNn]$ ]]; do
        read -p "${prefix}${@}${suffix}" confirm
        _prefix="Invalid input! "
        ! [[ "${confirm}" =~ [nN] ]]
    done
}

function get_user_input_path(){
    local msg=$1
    local fallback=$2
    local input
    read -e -p "${msg} [${fallback}]: " input
    echo ${input:-${fallback}}
}

function print_source_info() {
cat << EOF

## Sources
This tarball only contains pre-compiled binaries.

The full source code is available on

https://github.com/enguerrand/logrifle

EOF
}

function print_default_changelog(){
cat << EOF
<h4>New features</h4>
<ul>
    <li></li>
</ul>
<h4>Bugs fixed</h4>
<ul>
    <li></li>
</ul>
<h4>Known issues</h4>
<ul>
    <li>None</li>
</ul>
EOF
}


if [ "x$1" == "x-h" ] || [ "x$1" == "x--help" ];then
    print_usage
    exit 0
fi

cd $(dirname $0)

if [ $(git status --porcelain | wc -l) -ne 0 ]; then
    abort "You have uncommitted changes!"
fi

dst="./release"
version=$(tail -n +2 pom.xml | grep version | head -n 1 | sed -e 's/.*<version>\(.*\)<\/version>.*/\1/g')
jar="./target/logrifle.jar"
deb="./target/logrifle_${version}_all.deb"
license="./LICENSE"
third="./THIRD-PARTY.txt"
readme="./README.md"
released="${dst}/released.txt"
changes="${dst}/changes.php"
build_name="logrifle-${version}"
build_dir=${dst}/${build_name}
tarball=${build_name}.tar.gz

mkdir -pv ${dst}

changelog_template=$(mktemp)
print_default_changelog > ${changelog_template}
if [ -f ${changes} ]; then
    if ask_y "Existing changelog found. Keep as template?"; then
        cp -f ${changes} ${changelog_template}
    fi
fi

rm -rfv ${dst}/*

mkdir -pv ${build_dir}
cp -vt ${build_dir}/ ${license} ${third} ${readme} ${jar}
cp -vt ${dst}/ ${deb}
print_source_info >> ${build_dir}/${readme}
(cd ${dst} && tar -cvzf ${tarball} ${build_name})
(cd ${dst} && sha256sum ${tarball} > sha256sum.txt)
rm -rfv ${build_dir}
(cd ${dst} && sha256sum $(basename ${deb}) > deb.sha256sum.txt)
date > ${released}
mv ${changelog_template} ${changes}
echo ""
echo "Now edit changelog"
sleep 1
${EDITOR:-vim} ${changes}

final_dst=$(get_user_input_path "Please enter destination parent directory for this build" "../logrifle.de/dl/")
[[ "${final_dst}" =~ .*/$ ]] || final_dst+="/"
release_dir="${final_dst}${version}"
if [ -d "${release_dir}" ]; then
    ask_y "Directory ${release_dir} already exists! Abort?" && exit 0
    rm -rf "${release_dir}"
elif [ -d "${release_dir}-SNAPSHOT" ]; then
    ask_y "Found a SNAPSHOT for the version to be released: ${release_dir}-SNAPSHOT. Remove?" && \
        rm -rf "${release_dir}-SNAPSHOT"
fi

rsync -av ${dst}/ "${final_dst}${version}"
chmod 755 "${final_dst}${version}"
chmod 644 "${final_dst}${version}/"*
echo "Done. The release folder is now at ${final_dst}${version}"
