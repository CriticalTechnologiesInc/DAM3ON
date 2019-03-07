repo_path=$1
cd $repo_path
cd .git
remote_path=$(grep "annex-tahoe" config | awk '{print $3}')
cd $remote_path
nickname=$2
n=$(grep "nickname = [a-z,A-Z,0-9,-]*" -E  tahoe.cfg)
perl -pi -e "s/$n/nickname = $nickname/g" tahoe.cfg 
port=$3
p=$(grep "tcp:[0-9]{1,5}:interface" -Eo  tahoe.cfg)
perl -pi -e "s/$p/tcp:$port:interface/g" tahoe.cfg 
needed=$4
s1=$(grep "shares.needed" tahoe.cfg)
perl -pi -e "s/$s1/shares.needed = $needed/g" tahoe.cfg 
happy=$5
s2=$(grep "shares.happy" tahoe.cfg)
perl -pi -e "s/$s2/shares.happy = $happy/g" tahoe.cfg
total=$6
s3=$(grep "shares.total" tahoe.cfg)
perl -pi -e "s/$s3/shares.total = $total/g" tahoe.cfg
tahoe restart .
