job("jobt6-job1") {
	description("launch k8s container, attach pvc, expose it")
	
	scm {
		github('IshanSinghal/devt6','master')
	}
	
	triggers {
		scm("* * * * *")
    }
	
	steps {
	
		shell("""
if sudo ls . | grep -E 'html|php'
then
sudo cp -r /root/devt6/DockerfileWeb/Dockerfile .
else
sudo cp -r /root/devt6/DockerfilePy/Dockerfile .
fi

echo "done copy dockerfile"
""")
	
		shell("""
sudo docker build -t ishandsce/task6os .
echo "build done"

sudo docker push ishandsce/task6os
echo "push done"
""")
	
		shell("""

if sudo ls . | grep -E 'html|php'
then
sudo kubectl apply -k /root/devt6/yaml-web/.
sudo kubectl cp index.php default/task6-pods:/var/www/html/
else
sudo kubectl apply -k /root/devt6/yaml-py/.
sudo kubectl cp index.py default/task6-pods:/app/.
fi

""")

	}

}


job("jobt6-job3") {
	description("testing")
	
	triggers {
		upstream("jobt6-job1", String threshold = 'SUCCESS')
	}
	
	steps {
	
		shell("""
status=\$(sudo curl -o /dev/null -w "%{http_code}" -s 192.168.99.101:30000/)
if [[ \$status == 200 ]]
then
echo "Running fine!"
else
echo "Problem!"
exit 1
fi
""")

	}
	
	postBuildPublishers {
		extendedEmail {
			triggers {
				unstable {
					attachBuildLog(boolean attachBuildLog = true)
					recipientList("ishandsce@gmail.com")
					subject("App not running")
				}
			}
		}

	}

}

deliveryPipelineView("devt6") {
	allowPipelineStart(boolean value = true)
	allowRebuild(boolean value = true)
	columns(1)
	pipelineInstances(3)
	pipelines {
		component("devt6pipe", "devt6-job2")
	}
	updateInterval(2)
	
}






