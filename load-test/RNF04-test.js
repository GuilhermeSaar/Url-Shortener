import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 5 },   // Ramp-up: até 5 usuários concorrentes
    { duration: '2m', target: 20 },   // Carga sustentada: 20 usuários (equivalente a picos de tráfego)
    { duration: '30s', target: 0 },   // Ramp-down: encerramento gradual
  ],
  thresholds: {
    http_req_duration: ['p(95)<250'], // 95% das requisições devem responder em menos de 250ms
    http_req_failed: ['rate<0.01'],   // Taxa de erro menor que 1%
  },
};

const BASE_URL = 'http://localhost:8080/shorten';

export default function () {
  const payload = JSON.stringify({
    url: `https://example.com/very/long/url/path/${Math.random()}`,
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post(BASE_URL, payload, params);

  check(res, {
    'status é 201 Created': (r) => r.status === 201,
    'retorna o objeto JSON com a url encurtada': (r) => {
      try {
        return r.json('response') !== undefined;
      } catch (e) {
        return false;
      }
    },
  });

  sleep(1);
}
