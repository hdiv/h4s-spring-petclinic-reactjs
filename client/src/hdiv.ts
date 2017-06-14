
export interface SecureIdentifiable {
  nid?: string;
  id: string;
}

const Hdiv = {
  nid(id: string) {
    return id.substring(0, id.indexOf('-'));
  },

  hid(elements: SecureIdentifiable[], nid: string) {
    return elements.find((i) => i.nid === nid).id;
  }
};

export default Hdiv;
