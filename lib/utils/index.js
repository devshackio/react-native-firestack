// modeled after base64 web-safe chars, but ordered by ASCII
const PUSH_CHARS = '-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz';

const DEFAULT_CHUNK_SIZE = 50;

// internal promise handler
const _handler = (resolve, reject, err, resp) => {
  // resolve / reject after events etc
  setImmediate(() => {
    if (err) return reject(err);
    return resolve(resp);
  });
};


/**
 * Makes an objects keys it's values
 * @param object
 * @returns {{}}
 */
export function reverseKeyValues(object: Object): Object {
  const output = {};
  for (const key in object) {
    output[object[key]] = key;
  }
  return output;
}

/**
 * No operation func
 */
export function noop(): void {
}

/**
 * Wraps a native module method to support promises.
 * @param fn
 * @param NativeModule
 */
export function promisify(fn: Function, NativeModule: Object): Function<Promise> {
  return (...args) => {
    return new Promise((resolve, reject) => {
      const _fn = typeof fn === 'function' ? fn : NativeModule[fn];
      if (!_fn || typeof _fn !== 'function') return reject(new Error('Missing function for promisify.'));
      return _fn.apply(NativeModule, [...args, _handler.bind(_handler, resolve, reject)]);
    });
  };
}


/**
 * Delays chunks based on sizes per event loop.
 * @param collection
 * @param chunkSize
 * @param operation
 * @param callback
 * @private
 */
function _delayChunk(collection, chunkSize, operation, callback): void {
  const length = collection.length;
  const iterations = Math.ceil(length / chunkSize);

  // noinspection ES6ConvertVarToLetConst
  let thisIteration = 0;

  setImmediate(function next() {
    const start = thisIteration * chunkSize;
    const _end = start + chunkSize;
    const end = _end >= length ? length : _end;
    const result = operation(collection.slice(start, end), start, end);

    if (thisIteration++ > iterations) {
      callback(null, result);
    } else {
      setImmediate(next);
    }
  });
}

/**
 * Async each with optional chunk size limit
 * @param array
 * @param chunkSize
 * @param iterator
 * @param cb
 */
export function each(array: Array, chunkSize?: number, iterator: Function, cb: Function): void {
  if (typeof chunkSize === 'function') {
    cb = iterator;
    iterator = chunkSize;
    chunkSize = DEFAULT_CHUNK_SIZE;
  }

  _delayChunk(array, chunkSize, (slice, start) => {
    for (let ii = 0, jj = slice.length; ii < jj; ii += 1) {
      iterator(slice[ii], start + ii);
    }
  }, cb);
}

/**
 * Async map with optional chunk size limit
 * @param array
 * @param chunkSize
 * @param iterator
 * @param cb
 * @returns {*}
 */
export function map(array: Array, chunkSize?: number, iterator: Function, cb: Function): void {
  if (typeof chunkSize === 'function') {
    cb = iterator;
    iterator = chunkSize;
    chunkSize = DEFAULT_CHUNK_SIZE;
  }

  const result = [];
  _delayChunk(array, chunkSize, (slice, start) => {
    for (let ii = 0, jj = slice.length; ii < jj; ii += 1) {
      result.push(iterator(slice[ii], start + ii, array));
    }
    return result;
  }, () => cb(result));
}


// timestamp of last push, used to prevent local collisions if you push twice in one ms.
let lastPushTime = 0;

// we generate 72-bits of randomness which get turned into 12 characters and appended to the
// timestamp to prevent collisions with other clients.  We store the last characters we
// generated because in the event of a collision, we'll use those same characters except
// "incremented" by one.
const lastRandChars = [];

/**
 * Generate a firebase id - for use with ref().push(val, cb) - e.g. -KXMr7k2tXUFQqiaZRY4'
 * @param serverTimeOffset - pass in server time offset from native side
 * @returns {string}
 */
export function generatePushID(serverTimeOffset?: number = 0): string {
  const timeStampChars = new Array(8);
  let now = new Date().getTime() + serverTimeOffset;
  const duplicateTime = (now === lastPushTime);

  lastPushTime = now;

  for (let i = 7; i >= 0; i -= 1) {
    timeStampChars[i] = PUSH_CHARS.charAt(now % 64);
    now = Math.floor(now / 64);
  }

  if (now !== 0) throw new Error('We should have converted the entire timestamp.');

  let id = timeStampChars.join('');

  if (!duplicateTime) {
    for (let i = 0; i < 12; i += 1) {
      lastRandChars[i] = Math.floor(Math.random() * 64);
    }
  } else {
    // if the timestamp hasn't changed since last push,
    // use the same random number, but increment it by 1.
    let i;
    for (i = 11; i >= 0 && lastRandChars[i] === 63; i -= 1) {
      lastRandChars[i] = 0;
    }

    lastRandChars[i] += 1;
  }

  for (let i = 0; i < 12; i++) {
    id += PUSH_CHARS.charAt(lastRandChars[i]);
  }

  if (id.length !== 20) throw new Error('Length should be 20.');

  return id;
}
