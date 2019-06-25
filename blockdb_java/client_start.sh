echo "Simply push 100 transactions to server id 1,"
for I in `seq 0 9`; do
    for J in `seq 0 9`; do
	    go run ./test_client.go -T=TRANSFER --from USER00$I$J --to USER0099 --value=5 --fee=1
    done
done