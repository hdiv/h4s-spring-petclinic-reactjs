
export interface SecureIdentifiable {
  nid?: string;
  id: string;
}

const regex = new RegExp('(.+)-([0-9a-fA-F]{3})-(.{8}-([0-9a-fA-FU]{1,33})-\\d+-.+)');

const Hdiv = {
  nid(id: string) {
    return id.substring(0, id.indexOf('-'));
  },

  isHid(hid: string) {
    return regex.test(hid);
  }
};

export default Hdiv;

(function(window, document){

  let interceptors = [{
    request: function (url, config) {
      let headers = config ? config.headers : {};
      let hdivConfig = Object.assign({}, config, {
        credentials: 'include',
        headers: Object.assign({}, headers, {
          'X-HDIV-RANDOM': getCookie('HDIV_RANDOM_COOKIE')
        })
      });
      return [url, hdivConfig];
    }
  }];

  function interceptor(fetch, ...args) {
    const reversedInterceptors = interceptors.reduce((array, interceptor) => [interceptor].concat(array), []);
    let promise = Promise.resolve(args);

    reversedInterceptors.forEach(({ request, requestError }) => {
      if (request || requestError) {
        promise = promise.then(args => request(...args), requestError);
      }
    });

    promise = promise.then(args => fetch(...args));

    reversedInterceptors.forEach(({ response, responseError }) => {
      if (response || responseError) {
        promise = promise.then(response, responseError);
      }
    });

    return promise;
  };

  attach(window);

  function attach(env) {
    if (!env.fetch) {
      console.error('No fetch available.');
      return;
    }
    env.fetch = (function (fetch) {
      return function (...args) {
        return interceptor(fetch, ...args);
      };
    })(env.fetch);
  };


  const getCookie = function(c_name) {
    let c_value = ' ' + document.cookie;
    let c_start = c_value.indexOf(' ' + c_name + '=');
    if (c_start === -1) {
      c_value = null;
    }
    else {
      c_start = c_value.indexOf('=', c_start) + 1;
      let c_end = c_value.indexOf(';', c_start);
      if (c_end === -1) {
          c_end = c_value.length;
      }
      c_value = window['unescape'](c_value.substring(c_start, c_end));
    }
    return c_value;
  };

})(window, document);
