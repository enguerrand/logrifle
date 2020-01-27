#!/bin/bash
set -e
function print_usage(){
    cat << EOF
Usage: $(basename $0)
EOF
}
function abort(){
    echo "Error: $@" >&2
    print_usage
    exit -1
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


function print_default_changelog(){
cat << EOF
<h4>New features</h4>
<ul>
    <li></li>
</ul>
<h4>Bugfixes</h4>
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

dst="./release"
jar="./target/logrifle.jar"
license="./LICENSE"
third="./THIRD-PARTY.txt"
readme="./README.md"
released="${dst}/released.txt"
changes="${dst}/changes.php"
version=$(tail -n +2 pom.xml | grep version | head -n 1 | sed -e 's/.*<version>\(.*\)<\/version>.*/\1/g')
build_name="logrifle-${version}"
build_dir=${dst}/${version}

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
tar -cvzf ${dst}/${build_name}.tar.gz ${build_dir}
rm -rfv ${build_dir}
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
fi

rsync -av ${dst}/ "${final_dst}${version}"
echo "Done. The release folder is now at ${final_dst}${version}"