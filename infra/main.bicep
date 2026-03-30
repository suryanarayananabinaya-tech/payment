@description('Location for all resources')
param location string = resourceGroup().location

@description('AKS cluster name')
param aksName string

@description('ACR name (must be globally unique, 5-50 alphanumeric)')
param acrName string

@description('Key Vault name (must be globally unique)')
param kvName string

@description('AKS DNS prefix')
param dnsPrefix string = 'payaksdev'

@description('Node count')
param nodeCount int = 2

@description('Node VM size')
param nodeVmSize string = 'Standard_D2s_v3'

@description('Log Analytics retention in days')
param logRetentionDays int = 30

// ---------------------------
// Log Analytics
// ---------------------------
resource law 'Microsoft.OperationalInsights/workspaces@2023-09-01' = {
  name: '${aksName}-law'
  location: location
  properties: {
    sku: {
      name: 'PerGB2018'
    }
    retentionInDays: logRetentionDays
  }
}

// ---------------------------
// ACR
// ---------------------------
resource acr 'Microsoft.ContainerRegistry/registries@2023-07-01' = {
  name: acrName
  location: location
  sku: { name: 'Basic' }
  properties: {
    adminUserEnabled: false
  }
}

// ---------------------------
// Key Vault (RBAC enabled)
// ---------------------------
resource kv 'Microsoft.KeyVault/vaults@2023-07-01' = {
  name: kvName
  location: location
  properties: {
    tenantId: subscription().tenantId
    enableRbacAuthorization: true
    sku: { family: 'A', name: 'standard' }
    accessPolicies: []
  }
}

// ---------------------------
// AKS (OIDC + Workload Identity + KV CSI addon + Container Insights)
// ---------------------------
resource aks 'Microsoft.ContainerService/managedClusters@2024-03-02-preview' = {
  name: aksName
  location: location
  identity: {
    type: 'SystemAssigned'
  }
  properties: {
    dnsPrefix: dnsPrefix

    // ✅ Fix 1: property is issuerURL (not issuerUrl)
    oidcIssuerProfile: {
      enabled: true
    }

    securityProfile: {
      workloadIdentity: {
        enabled: true
      }
    }

    agentPoolProfiles: [
      {
        name: 'system'
        mode: 'System'
        count: nodeCount
        vmSize: nodeVmSize
        osType: 'Linux'
        type: 'VirtualMachineScaleSets'
      }
    ]

    addonProfiles: {
      omsagent: {
        enabled: true
        config: {
          logAnalyticsWorkspaceResourceID: law.id
        }
      }
      azureKeyvaultSecretsProvider: {
        enabled: true
      }
    }

    networkProfile: {
      networkPlugin: 'azure'
      loadBalancerSku: 'standard'
    }
  }
}

// ---------------------------
// Allow AKS kubelet identity to pull from ACR
// ---------------------------
var kubeletObjectId = aks.properties.identityProfile.kubeletidentity.objectId

resource acrPullRole 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  // ✅ Fix 2: roleAssignment name must be deterministic at start of deployment
  // Use only values known at the start: acr.id + aksName + constant string
  name: guid(acr.id, aksName, 'acrpull')
  scope: acr
  properties: {
    roleDefinitionId: subscriptionResourceId(
      'Microsoft.Authorization/roleDefinitions',
      '7f951dda-4ed3-4680-a7ca-43fe172d538d' // AcrPull
    )
    // principalId can be runtime; that's OK
    principalId: kubeletObjectId
    principalType: 'ServicePrincipal'
  }
}

// ---------------------------
// Outputs
// ---------------------------
output acrLoginServer string = acr.properties.loginServer
output aksNameOut string = aks.name
output keyVaultName string = kv.name
output tenantId string = subscription().tenantId
output logAnalyticsWorkspaceId string = law.id

// ✅ Fix 1 also affects output property name:
output oidcIssuerUrl string = aks.properties.oidcIssuerProfile.issuerURL