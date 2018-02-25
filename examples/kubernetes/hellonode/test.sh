rm -f out.log; touch out.log;
while true; do 
	# get all pods
	kubectl get pods -o go-template --template '{{range .items}}{{.metadata.name}}{{"\n"}}{{end}}' | \

	# count number of responses in the log for each pod
	while read line; do 
		echo -n $line; grep $line out.log | wc -l; 
	done;

	# fire requests at the service
	# replace IP with your minikube IP and port with the service port
	# you can get this information by running minikube service hello-node
	for i in `seq 1 100`; do 
		curl --silent http://192.168.99.100:30792/ >>out.log; 
		echo "" >>out.log; 
	done;

	echo "===";

	sleep 0.5; 
done
