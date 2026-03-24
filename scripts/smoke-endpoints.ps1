param(
    [ValidateSet('dev','qa','release','main','all')]
    [string]$Environment = 'dev'
)

$portsByEnv = @{
    dev = @{ config=8888; eureka=8761; customer=8091; product=8092; cart=8093 }
    qa = @{ config=18888; eureka=18761; customer=18091; product=18092; cart=18093 }
    release = @{ config=28888; eureka=28761; customer=28091; product=28092; cart=28093 }
    main = @{ config=38888; eureka=38761; customer=38091; product=38092; cart=38093 }
}

function Get-StatusCode {
    param([string]$Url)
    try {
        return (Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 10).StatusCode
    }
    catch {
        if ($_.Exception.Response) {
            return [int]$_.Exception.Response.StatusCode
        }
        return 'ERR'
    }
}

function Test-Environment {
    param([string]$Env)

    $p = $portsByEnv[$Env]
    if (-not $p) {
        throw "Unknown environment: $Env"
    }

    Write-Host "===== Smoke checks: $Env ====="

    $checks = @(
        @{ name='config-health'; url="http://localhost:$($p.config)/actuator/health"; expected=@('200') },
        @{ name='eureka-health'; url="http://localhost:$($p.eureka)/actuator/health"; expected=@('200') },
        @{ name='customer-list'; url="http://localhost:$($p.customer)/api/v1/customers"; expected=@('200') },
        @{ name='product-list'; url="http://localhost:$($p.product)/api/v1/products"; expected=@('200') },
        @{ name='cart-list'; url="http://localhost:$($p.cart)/api/v1/carts"; expected=@('200','404') }
    )

    $failed = $false
    foreach ($check in $checks) {
        $code = [string](Get-StatusCode -Url $check.url)
        if ($check.expected -contains $code) {
            Write-Host "[OK] $($check.name) -> $code ($($check.url))"
        }
        else {
            Write-Host "[FAIL] $($check.name) -> $code ($($check.url)), expected: $($check.expected -join ',')"
            $failed = $true
        }
    }

    if ($failed) {
        throw "Smoke checks failed for environment: $Env"
    }

    Write-Host "Smoke checks passed for environment: $Env"
}

if ($Environment -eq 'all') {
    foreach ($envName in @('dev','qa','release','main')) {
        Test-Environment -Env $envName
    }
}
else {
    Test-Environment -Env $Environment
}

