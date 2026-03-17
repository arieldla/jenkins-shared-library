# Jenkins Shared Library

A Groovy shared library for Jenkins pipelines — reusable steps for Terraform operations,
AWS authentication via IAM Roles Anywhere, Trivy security scanning, and notifications.

Instead of copy-pasting the same pipeline logic across every repo, import this library
and call standardized steps. One change here propagates to every pipeline that uses it.

## Why Shared Libraries

Without a shared library:
- Every repo has its own copy of the Trivy scan logic
- When you update the scan command, you update 5 repos
- Pipelines drift — some repos run old patterns, some run new ones

With this shared library:
- One source of truth for all pipeline steps
- Update once, all pipelines pick it up on next run
- Enforces consistent security and quality gates across all repos

## Library Structure

```
vars/
|-- awsAuth.groovy          # IAM Roles Anywhere credential exchange
|-- terraformPlan.groovy    # terraform init + validate + plan with output archiving
|-- terraformApply.groovy   # terraform apply with approval gate
|-- trivyScan.groovy        # Trivy filesystem, IaC, and secret scanning
|-- notifySlack.groovy      # Build status notifications
`-- approvalGate.groovy     # Parameterized manual approval step

src/
`-- com/dlagroup/
    |-- AwsCredentials.groovy   # Credential helper class
    `-- TerraformUtils.groovy   # Terraform output parsing utilities

resources/
`-- trivy-config.yaml           # Shared Trivy configuration
```

## Usage

### Configure the Library in Jenkins

In **Manage Jenkins > Configure System > Global Pipeline Libraries:**

| Field | Value |
|---|---|
| Name | `dla-shared-library` |
| Default version | `main` |
| Source | GitHub/Gitea URL for this repo |

### Import in a Jenkinsfile

```groovy
@Library('dla-shared-library') _

pipeline {
    agent any
    stages {
        stage('AWS Auth') {
            steps {
                awsAuth(
                    trustAnchorArn: 'arn:aws:rolesanywhere:us-east-1:640168421612:trust-anchor/...',
                    profileArn:     'arn:aws:rolesanywhere:us-east-1:640168421612:profile/...',
                    roleArn:        'arn:aws:iam::640168421612:role/JenkinsLabRole'
                )
            }
        }
        stage('Security Scan') {
            steps {
                trivyScan(severity: 'CRITICAL,HIGH', failOnFindings: true)
            }
        }
        stage('Terraform Plan') {
            steps {
                terraformPlan(environment: params.ENVIRONMENT, backendBucket: 'dla-tstate')
            }
        }
        stage('Terraform Apply') {
            steps {
                terraformApply(environment: params.ENVIRONMENT, requireApproval: true)
            }
        }
    }
}
```

## Available Steps

| Step | Parameters | Description |
|---|---|---|
| `awsAuth` | trustAnchorArn, profileArn, roleArn | Exchanges X.509 cert for STS session via IAM Roles Anywhere |
| `terraformPlan` | environment, backendBucket | Runs init + validate + plan, archives output |
| `terraformApply` | environment, requireApproval | Applies with optional approval gate |
| `trivyScan` | severity, failOnFindings | Runs Trivy across fs, config, and secrets |
| `approvalGate` | message, timeoutMinutes | Pauses pipeline for human approval |
| `notifySlack` | channel, status, message | Posts build status to Slack |

## Exam Relevance

| Cert | Concept |
|---|---|
| AWS SCS-C03 | Consistent security controls, pipeline hardening |
| AZ-104 | Reusable automation patterns, change management |

## Related Repos

- [jenkins-iam-roles-anywhere](https://github.com/arieldla/jenkins-iam-roles-anywhere) — Auth layer these steps wrap
- [cicd-golden-pipeline](https://github.com/arieldla/cicd-golden-pipeline) — Pipeline that consumes this library
- [jenkins-labs](https://github.com/arieldla/jenkins-labs) — Lab 7 introduces this library pattern
