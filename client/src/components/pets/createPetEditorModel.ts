import { IPetType, ISecureIdentifiableOption } from '../../types';
import { url, submitForm } from '../../util';

const toSelectOptions = (pettypes: IPetType[]): ISecureIdentifiableOption[] => pettypes.map(pettype => ({ value: pettype.nid, nid: pettype.nid, id: pettype.id, name: pettype.name }));

export default (ownerId: string, petLoaderPromise: Promise<any>): Promise<any> => {
  return Promise.all(
    [fetch(url('/api/pettypes'))
      .then(response => response.json())
      .then(toSelectOptions),
    fetch(url('/api/owner/' + ownerId))
      .then(response => response.json()),
      petLoaderPromise,
    ]
  ).then(results => ({
    pettypes: results[0],
    owner: results[1],
    pet: results[2]
  }));
};
