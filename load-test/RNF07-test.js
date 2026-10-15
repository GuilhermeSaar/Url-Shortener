import http from 'k6/http';
import { check } from 'k6';

// Exemplo de execução:
// BASE_URL=http://localhost:8080 SHORT_CODE=WXftDz k6 run load-test/RNF07-test.js
const BASE_URL = (__ENV.BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
const SHORT_CODE = __ENV.SHORT_CODE || 'WXftDz';

export const options = {
  scenarios: {
    redirecionamentos: {
      executor: 'constant-arrival-rate',
      rate: 50,
      timeUnit: '1s',
      duration: '2m',
      preAllocatedVUs: 20,
      maxVUs: 100,
    },
  },
  thresholds: {
    'http_req_duration{endpoint:redirecionamento}': ['p(95)<50'],
    'http_req_failed{endpoint:redirecionamento}': ['rate<0.01'],
    checks: ['rate>0.99'],
  },
};

export default function () {
  const response = http.get(`${BASE_URL}/${SHORT_CODE}`, {
    // Mede somente a resposta 302 do encurtador, não a página de destino.
    redirects: 0,
    tags: { endpoint: 'redirecionamento' },
  });

  check(response, {
    'retorna 302 Found': (res) => res.status === 302,
    'inclui cabecalho Location': (res) => Boolean(res.headers.Location || res.headers.location),
  });
}
