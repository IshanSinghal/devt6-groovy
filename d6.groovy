job("jobt6-job1") {
	description("launch k8s container, attach pvc, expose it")
	scm {
		github('','master')
	}
}
