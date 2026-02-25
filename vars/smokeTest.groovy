def call(String containerName) {
    def result = sh(
        script: "docker ps | grep ${containerName}",
        returnStatus: true
    )

    if (result != 0) {
        error "❌ Smoke test FAILED — container '${containerName}' is not running!"
    }
    echo "✅ Smoke test passed — ${containerName} is running!"
}
